
package com.arunya.smartstock.stockmovement.service;

import com.arunya.smartstock.batch.repository.InventoryBatchRepository;
import com.arunya.smartstock.batch.service.BatchAllocationService;

import com.arunya.smartstock.inventory.entity.StoreInventory;
import com.arunya.smartstock.inventory.repository.InventoryRepository;

import com.arunya.smartstock.product.repository.ProductRepository;
import com.arunya.smartstock.store.repository.StoreRepository;

import com.arunya.smartstock.stockmovement.dto.StockMovementRequest;
import com.arunya.smartstock.stockmovement.dto.StockMovementResponse;
import com.arunya.smartstock.stockmovement.entity.StockMovement;
import com.arunya.smartstock.stockmovement.entity.StockMovementType;
import com.arunya.smartstock.stockmovement.repository.StockMovementRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryBatchRepository batchRepository;
    private final BatchAllocationService allocationService;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    public StockMovementService(
            StockMovementRepository movementRepository,
            InventoryRepository inventoryRepository,
            InventoryBatchRepository batchRepository,
            BatchAllocationService allocationService,
            StoreRepository storeRepository,
            ProductRepository productRepository
    ) {
        this.movementRepository = movementRepository;
        this.inventoryRepository = inventoryRepository;
        this.batchRepository = batchRepository;
        this.allocationService = allocationService;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public StockMovementResponse create(
            StockMovementRequest request
    ) {

        StoreInventory inventory = inventoryRepository
                .findByIdForUpdate(request.inventoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found"));

        if (!Boolean.TRUE.equals(inventory.getActive())) {
            throw error("Inventory is inactive");
        }

        validateStoreAndProduct(inventory);

        LocalDate today = LocalDate.now(
                ZoneId.of("America/Vancouver"));

        BigDecimal change = calculateChange(
                request.movementType(),
                request.quantity()
        );

        BigDecimal before = inventory.getQuantityOnHand();
        BigDecimal after = before.add(change);

        BigDecimal batchTotal =
                batchRepository.findTotalQuantity(inventory.getId());

        if (batchTotal.compareTo(before) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inventory and batch quantities are inconsistent");
        }

        if (after.signum() < 0) {
            throw error("Insufficient physical stock");
        }

        if (after.compareTo(inventory.getQuantityReserved()) < 0) {
            throw error("Cannot reduce stock below reserved quantity");
        }

        if (after.precision() - after.scale() > 9
                || after.scale() > 3) {
            throw error("Quantity exceeds database precision");
        }

        if (request.movementType() == StockMovementType.SALE) {

            BigDecimal sellable = batchRepository.findSellableQuantity(
                    inventory.getId(), today);

            BigDecimal available = sellable.subtract(
                    inventory.getQuantityReserved());

            if (request.quantity().compareTo(available) > 0) {
                throw error("Insufficient sellable stock");
            }
        }

        StockMovement movement = new StockMovement();

        movement.setInventoryId(inventory.getId());
        movement.setMovementType(request.movementType());
        movement.setQuantityChange(change);
        movement.setQuantityBefore(before);
        movement.setQuantityAfter(after);
        movement.setReferenceNumber(request.referenceNumber());
        movement.setReason(request.reason());

        StockMovement saved =
                movementRepository.saveAndFlush(movement);

        applyBatchMovement(
                request,
                change,
                saved.getId(),
                today
        );

        BigDecimal updatedBatchTotal =
                batchRepository.findTotalQuantity(inventory.getId());

        if (updatedBatchTotal.compareTo(after) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Batch reconciliation failed");
        }

        BigDecimal sellableAfter =
                batchRepository.findSellableQuantity(
                        inventory.getId(), today);

        if (sellableAfter.compareTo(
                inventory.getQuantityReserved()) < 0) {
            throw error("Movement would leave insufficient sellable stock for reservations");
        }

        inventory.setQuantityOnHand(after);
        inventoryRepository.saveAndFlush(inventory);

        return StockMovementResponse.from(saved);
    }

    private void applyBatchMovement(
            StockMovementRequest request,
            BigDecimal change,
            Long stockMovementId,
            LocalDate today
    ) {

        switch (request.movementType()) {

            case RECEIVE, RETURN -> allocationService.receive(
                    request.inventoryId(),
                    request.batchNumber(),
                    change,
                    request.manufacturedDate(),
                    request.expiryDate(),
                    stockMovementId,
                    today
            );

            case SALE -> allocationService.allocateFEFO(
                    request.inventoryId(),
                    change.abs(),
                    stockMovementId,
                    today
            );

            case DAMAGE -> allocationService.deductFromBatch(
                    request.inventoryId(),
                    request.batchNumber(),
                    change.abs(),
                    stockMovementId
            );

            case ADJUSTMENT -> {

                if (change.signum() > 0) {

                    allocationService.receive(
                            request.inventoryId(),
                            request.batchNumber(),
                            change,
                            request.manufacturedDate(),
                            request.expiryDate(),
                            stockMovementId,
                            today
                    );

                } else {

                    allocationService.deductFromBatch(
                            request.inventoryId(),
                            request.batchNumber(),
                            change.abs(),
                            stockMovementId
                    );
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public StockMovementResponse findById(Long id) {

        return movementRepository.findById(id)
                .map(StockMovementResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stock movement not found"));
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> findByInventory(
            Long inventoryId
    ) {

        if (!inventoryRepository.existsById(inventoryId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Inventory not found");
        }

        return movementRepository
                .findByInventoryIdOrderByCreatedAtDescIdDesc(inventoryId)
                .stream()
                .map(StockMovementResponse::from)
                .toList();
    }

    private BigDecimal calculateChange(
            StockMovementType type,
            BigDecimal quantity
    ) {

        if (type == null) {
            throw error("Movement type is required");
        }

        if (quantity == null || quantity.signum() == 0) {
            throw error("Quantity must not be zero");
        }

        if (quantity.scale() > 3
                || quantity.precision() - quantity.scale() > 9) {
            throw error("Invalid quantity precision");
        }

        if (type != StockMovementType.ADJUSTMENT
                && quantity.signum() < 0) {
            throw error("Quantity must be positive");
        }

        return switch (type) {
            case RECEIVE, RETURN -> quantity;
            case SALE, DAMAGE -> quantity.negate();
            case ADJUSTMENT -> quantity;
        };
    }

    private void validateStoreAndProduct(StoreInventory inventory) {

        var store = storeRepository.findById(inventory.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Store not found"));

        var product = productRepository.findById(
                        inventory.getProductId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"));

        if (!store.isActive() || !product.isActive()) {
            throw error("Store or product is inactive");
        }
    }

    private ResponseStatusException error(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }
}
