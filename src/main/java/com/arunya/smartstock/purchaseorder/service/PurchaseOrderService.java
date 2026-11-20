package com.arunya.smartstock.purchaseorder.service;


import com.arunya.smartstock.purchaseorder.dto.*;
import com.arunya.smartstock.purchaseorder.entity.*;
import com.arunya.smartstock.purchaseorder.repository.*;

import com.arunya.smartstock.supplier.repository.SupplierRepository;
import com.arunya.smartstock.store.repository.StoreRepository;
import com.arunya.smartstock.product.repository.ProductRepository;

import com.arunya.smartstock.inventory.dto.InventoryRequest;
import com.arunya.smartstock.inventory.repository.InventoryRepository;
import com.arunya.smartstock.inventory.service.InventoryService;

import com.arunya.smartstock.stockmovement.dto.StockMovementRequest;
import com.arunya.smartstock.stockmovement.entity.StockMovementType;
import com.arunya.smartstock.stockmovement.service.StockMovementService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository orderRepository;
    private final PurchaseOrderItemRepository itemRepository;
    private final SupplierRepository supplierRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final StockMovementService stockMovementService;

    public PurchaseOrderService(
            PurchaseOrderRepository orderRepository,
            PurchaseOrderItemRepository itemRepository,
            SupplierRepository supplierRepository,
            StoreRepository storeRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            InventoryService inventoryService,
            StockMovementService stockMovementService
    ) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.supplierRepository = supplierRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryService = inventoryService;
        this.stockMovementService = stockMovementService;
    }

    @Transactional
    public PurchaseOrderResponse create(
            PurchaseOrderCreateRequest request
    ) {

        validateSupplier(request.supplierId());
        validateStore(request.storeId());

        Set<Long> productIds = new HashSet<>();

        for (var item : request.items()) {

            validateProduct(item.productId());

            if (!productIds.add(item.productId())) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate product in purchase order"
                );
            }
        }

        PurchaseOrder order = new PurchaseOrder();

        order.setOrderNumber(generateOrderNumber());
        order.setSupplierId(request.supplierId());
        order.setStoreId(request.storeId());
        order.setExpectedDeliveryDate(
                request.expectedDeliveryDate());
        order.setNotes(request.notes());
        order.setStatus(PurchaseOrderStatus.DRAFT);

        PurchaseOrder saved =
                orderRepository.saveAndFlush(order);

        List<PurchaseOrderItem> items = new ArrayList<>();

        for (var itemRequest : request.items()) {

            PurchaseOrderItem item = new PurchaseOrderItem();

            item.setPurchaseOrderId(saved.getId());
            item.setProductId(itemRequest.productId());
            item.setQuantityOrdered(itemRequest.quantity());
            item.setQuantityReceived(BigDecimal.ZERO);
            item.setUnitCost(itemRequest.unitCost());

            items.add(item);
        }

        List<PurchaseOrderItem> savedItems =
                itemRepository.saveAllAndFlush(items);

        return PurchaseOrderResponse.from(saved, savedItems);
    }

    @Transactional
    public PurchaseOrderResponse placeOrder(Long id) {

        PurchaseOrder order = getLocked(id);

        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Only draft purchase orders can be placed"
            );
        }

        validateSupplier(order.getSupplierId());
        validateStore(order.getStoreId());

        List<PurchaseOrderItem> items =
                itemRepository
                        .findByPurchaseOrderIdOrderByIdAsc(id);

        if (items.isEmpty()) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Purchase order must contain products"
            );
        }

        items.forEach(item ->
                validateProduct(item.getProductId()));

        order.setStatus(PurchaseOrderStatus.ORDERED);
        order.setOrderedAt(OffsetDateTime.now());

        return PurchaseOrderResponse.from(
                orderRepository.saveAndFlush(order),
                items
        );
    }

    @Transactional
    public PurchaseOrderResponse receive(
            Long id,
            PurchaseOrderReceiveRequest request
    ) {

        PurchaseOrder order = getLocked(id);

        if (order.getStatus() != PurchaseOrderStatus.ORDERED
                && order.getStatus()
                != PurchaseOrderStatus.PARTIALLY_RECEIVED) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Purchase order is not eligible for receiving"
            );
        }

        validateStore(order.getStoreId());

        List<PurchaseOrderItem> items =
                itemRepository
                        .findByPurchaseOrderIdOrderByIdAsc(id);

        Map<Long, PurchaseOrderItem> itemMap = new HashMap<>();

        for (PurchaseOrderItem item : items) {
            itemMap.put(item.getProductId(), item);
        }

        // Consistent ordering reduces inventory lock conflicts.
        List<PurchaseOrderReceiveItemRequest> deliveries =
                request.items()
                        .stream()
                        .sorted(Comparator.comparing(
                                PurchaseOrderReceiveItemRequest::productId))
                        .toList();

        for (var delivery : deliveries) {

            PurchaseOrderItem item =
                    itemMap.get(delivery.productId());

            if (item == null) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "Product is not part of the purchase order: "
                                + delivery.productId()
                );
            }

            validateProduct(delivery.productId());

            if (delivery.quantity().compareTo(
                    item.getQuantityOutstanding()) > 0) {

                throw error(
                        HttpStatus.BAD_REQUEST,
                        "Received quantity exceeds outstanding quantity"
                );
            }

            Long inventoryId = getOrCreateInventory(
                    order.getStoreId(),
                    delivery.productId()
            );

            StockMovementRequest movementRequest =
                    new StockMovementRequest(
                            inventoryId,
                            StockMovementType.RECEIVE,
                            delivery.quantity(),
                            order.getOrderNumber(),
                            "Purchase order delivery",
                            delivery.batchNumber(),
                            delivery.manufacturedDate(),
                            delivery.expiryDate()
                    );

            stockMovementService.create(movementRequest);

            item.setQuantityReceived(
                    item.getQuantityReceived()
                            .add(delivery.quantity())
            );

            itemRepository.save(item);
        }

        itemRepository.flush();

        boolean fullyReceived = items.stream()
                .allMatch(PurchaseOrderItem::isFullyReceived);

        if (fullyReceived) {

            order.setStatus(PurchaseOrderStatus.RECEIVED);
            order.setReceivedAt(OffsetDateTime.now());

        } else {

            order.setStatus(
                    PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }

        return PurchaseOrderResponse.from(
                orderRepository.saveAndFlush(order),
                items
        );
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {

        PurchaseOrder order = getLocked(id);

        if (order.getStatus() != PurchaseOrderStatus.DRAFT
                && order.getStatus() != PurchaseOrderStatus.ORDERED) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Purchase order cannot be cancelled"
            );
        }

        order.setStatus(PurchaseOrderStatus.CANCELLED);

        return toResponse(
                orderRepository.saveAndFlush(order));
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse findById(Long id) {

        PurchaseOrder order = orderRepository.findById(id)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Purchase order not found"
                ));

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> findAll() {

        return orderRepository
                .findAllByOrderByCreatedAtDescIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> findByStore(
            Long storeId
    ) {

        if (!storeRepository.existsById(storeId)) {
            throw error(
                    HttpStatus.NOT_FOUND,
                    "Store not found"
            );
        }

        return orderRepository
                .findByStoreIdOrderByCreatedAtDescIdDesc(storeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> findBySupplier(
            Long supplierId
    ) {

        if (!supplierRepository.existsById(supplierId)) {
            throw error(
                    HttpStatus.NOT_FOUND,
                    "Supplier not found"
            );
        }

        return orderRepository
                .findBySupplierIdOrderByCreatedAtDescIdDesc(
                        supplierId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Long getOrCreateInventory(
            Long storeId,
            Long productId
    ) {

        return inventoryRepository
                .findByStoreIdAndProductId(storeId, productId)
                .map(inventory -> inventory.getId())
                .orElseGet(() -> {

                    InventoryRequest request =
                            new InventoryRequest(
                                    storeId,
                                    productId,
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO
                            );

                    return inventoryService.create(request).id();
                });
    }

    private PurchaseOrder getLocked(Long id) {

        return orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Purchase order not found"
                ));
    }

    private PurchaseOrderResponse toResponse(
            PurchaseOrder order
    ) {

        return PurchaseOrderResponse.from(
                order,
                itemRepository
                        .findByPurchaseOrderIdOrderByIdAsc(
                                order.getId())
        );
    }

    private void validateSupplier(Long supplierId) {

        var supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Supplier not found"
                ));

        if (!supplier.isActive()) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Supplier is inactive"
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

    private void validateProduct(Long productId) {

        var product = productRepository.findById(productId)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        if (!product.isActive()) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Product is inactive"
            );
        }
    }

    private String generateOrderNumber() {

        return "PO-" + UUID.randomUUID()
                .toString()
                .toUpperCase(Locale.ROOT);
    }

    private ResponseStatusException error(
            HttpStatus status,
            String message
    ) {
        return new ResponseStatusException(status, message);
    }
}
