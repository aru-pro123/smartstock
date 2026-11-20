package com.arunya.smartstock.replenishment.service;


import com.arunya.smartstock.replenishment.dto.*;

import com.arunya.smartstock.inventory.entity.StoreInventory;
import com.arunya.smartstock.inventory.repository.InventoryRepository;

import com.arunya.smartstock.batch.repository.InventoryBatchRepository;

import com.arunya.smartstock.store.repository.StoreRepository;
import com.arunya.smartstock.product.repository.ProductRepository;

import com.arunya.smartstock.purchaseorder.dto.PurchaseOrderCreateRequest;
import com.arunya.smartstock.purchaseorder.dto.PurchaseOrderItemRequest;
import com.arunya.smartstock.purchaseorder.dto.PurchaseOrderResponse;
import com.arunya.smartstock.purchaseorder.entity.PurchaseOrderStatus;
import com.arunya.smartstock.purchaseorder.repository.PurchaseOrderItemRepository;
import com.arunya.smartstock.purchaseorder.service.PurchaseOrderService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
public class ReplenishmentService {

    private static final ZoneId STORE_ZONE =
            ZoneId.of("America/Vancouver");

    private static final List<PurchaseOrderStatus> INCOMING_STATUSES =
            List.of(
                    PurchaseOrderStatus.ORDERED,
                    PurchaseOrderStatus.PARTIALLY_RECEIVED
            );

    private final InventoryRepository inventoryRepository;
    private final InventoryBatchRepository batchRepository;
    private final PurchaseOrderItemRepository itemRepository;
    private final PurchaseOrderService purchaseOrderService;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    public ReplenishmentService(
            InventoryRepository inventoryRepository,
            InventoryBatchRepository batchRepository,
            PurchaseOrderItemRepository itemRepository,
            PurchaseOrderService purchaseOrderService,
            StoreRepository storeRepository,
            ProductRepository productRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.batchRepository = batchRepository;
        this.itemRepository = itemRepository;
        this.purchaseOrderService = purchaseOrderService;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ReplenishmentResponse> findByStore(
            Long storeId
    ) {

        validateStore(storeId);

        LocalDate today = LocalDate.now(STORE_ZONE);

        return inventoryRepository.findByStoreId(storeId)
                .stream()
                .filter(inventory ->
                        Boolean.TRUE.equals(inventory.getActive()))
                .filter(inventory ->
                        isProductActive(inventory.getProductId()))
                .map(inventory -> calculate(inventory, today))
                .filter(ReplenishmentResponse::replenishmentRequired)
                .sorted(Comparator
                        .comparing(ReplenishmentResponse::projectedQuantity)
                        .thenComparing(ReplenishmentResponse::inventoryId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReplenishmentResponse findByInventory(
            Long inventoryId
    ) {

        StoreInventory inventory = inventoryRepository
                .findById(inventoryId)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found"
                ));

        validateInventory(inventory);

        return calculate(
                inventory,
                LocalDate.now(STORE_ZONE)
        );
    }

    @Transactional
    public PurchaseOrderResponse generatePurchaseOrder(
            ReplenishmentOrderRequest request
    ) {

        StoreInventory inventory = inventoryRepository
                .findByIdForUpdate(request.inventoryId())
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found"
                ));

        validateInventory(inventory);

        LocalDate today = LocalDate.now(STORE_ZONE);

        // Recalculate using current inventory data.
        ReplenishmentResponse recommendation =
                calculate(inventory, today);

        if (!recommendation.replenishmentRequired()) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Replenishment is not required"
            );
        }

        long draftCount =
                itemRepository.countByStoreAndProductAndStatus(
                        inventory.getStoreId(),
                        inventory.getProductId(),
                        PurchaseOrderStatus.DRAFT
                );

        if (draftCount > 0) {
            throw error(
                    HttpStatus.CONFLICT,
                    "A draft purchase order already exists for this product"
            );
        }

        BigDecimal quantity =
                recommendation.recommendedQuantity();

        if (quantity.precision() - quantity.scale() > 9
                || quantity.scale() > 3) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Recommended quantity exceeds supported precision"
            );
        }

        PurchaseOrderItemRequest item =
                new PurchaseOrderItemRequest(
                        inventory.getProductId(),
                        quantity,
                        request.unitCost()
                );

        String notes = request.notes() == null
                || request.notes().isBlank()
                ? "Generated by SmartStock replenishment engine"
                : request.notes();

        PurchaseOrderCreateRequest orderRequest =
                new PurchaseOrderCreateRequest(
                        request.supplierId(),
                        inventory.getStoreId(),
                        request.expectedDeliveryDate(),
                        notes,
                        List.of(item)
                );

        return purchaseOrderService.create(orderRequest);
    }

    private ReplenishmentResponse calculate(
            StoreInventory inventory,
            LocalDate today
    ) {

        BigDecimal physical =
                inventory.getQuantityOnHand();

        BigDecimal reserved =
                inventory.getQuantityReserved();

        BigDecimal sellable =
                batchRepository.findSellableQuantity(
                        inventory.getId(),
                        today
                );

        BigDecimal batchTotal =
                batchRepository.findTotalQuantity(
                        inventory.getId());

        if (batchTotal.compareTo(physical) != 0) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Inventory and batch quantities are inconsistent"
            );
        }

        BigDecimal expired =
                physical.subtract(sellable);

        BigDecimal available =
                sellable.subtract(reserved)
                        .max(BigDecimal.ZERO);

        BigDecimal incoming =
                itemRepository.findIncomingQuantity(
                        inventory.getStoreId(),
                        inventory.getProductId(),
                        INCOMING_STATUSES
                );

        BigDecimal projected =
                sellable.subtract(reserved).add(incoming);

        BigDecimal reorderLevel =
                inventory.getReorderLevel();

        BigDecimal reorderQuantity =
                inventory.getReorderQuantity();

        BigDecimal target =
                reorderLevel.add(reorderQuantity);

        boolean lowStock =
                available.compareTo(reorderLevel) <= 0;

        BigDecimal recommended = BigDecimal.ZERO;

        if (reorderQuantity.signum() > 0
                && projected.compareTo(reorderLevel) <= 0) {

            recommended = target.subtract(projected)
                    .max(BigDecimal.ZERO);
        }

        boolean replenishmentRequired =
                recommended.signum() > 0;

        return new ReplenishmentResponse(
                inventory.getId(),
                inventory.getStoreId(),
                inventory.getProductId(),

                physical,
                expired,
                sellable,
                reserved,
                available,

                incoming,
                projected,

                reorderLevel,
                reorderQuantity,
                target,

                recommended,

                lowStock,
                replenishmentRequired
        );
    }

    private void validateInventory(
            StoreInventory inventory
    ) {

        if (!Boolean.TRUE.equals(inventory.getActive())) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Inventory is inactive"
            );
        }

        validateStore(inventory.getStoreId());

        if (!isProductActive(inventory.getProductId())) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Product is inactive"
            );
        }
    }

    private void validateStore(Long storeId) {

        var store = storeRepository.findById(storeId)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Store not found"
                ));

        if (!store.isActive()) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Store is inactive"
            );
        }
    }

    private boolean isProductActive(Long productId) {

        return productRepository.findById(productId)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ))
                .isActive();
    }

    private ResponseStatusException error(
            HttpStatus status,
            String message
    ) {
        return new ResponseStatusException(status, message);
    }
}
