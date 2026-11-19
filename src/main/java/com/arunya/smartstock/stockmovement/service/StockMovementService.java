package com.arunya.smartstock.stockmovement.service;


import com.arunya.smartstock.inventory.entity.StoreInventory;
import com.arunya.smartstock.inventory.repository.InventoryRepository;
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
import java.util.List;

@Service
public class StockMovementService {

    private final StockMovementRepository movementRepository;
    private final InventoryRepository inventoryRepository;

    public StockMovementService(
            StockMovementRepository movementRepository,
            InventoryRepository inventoryRepository
    ) {
        this.movementRepository = movementRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public StockMovementResponse create(StockMovementRequest request) {

        StoreInventory inventory = inventoryRepository
                .findByIdForUpdate(request.inventoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found"));

        if (!Boolean.TRUE.equals(inventory.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Inventory is inactive");
        }

        BigDecimal change = calculateChange(
                request.movementType(),
                request.quantity()
        );

        BigDecimal before = inventory.getQuantityOnHand();
        BigDecimal after = before.add(change);

        if (after.signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient physical stock");
        }

        if (after.compareTo(inventory.getQuantityReserved()) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot reduce stock below reserved quantity");
        }

        if (after.precision() - after.scale() > 9
                || after.scale() > 3) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stock quantity exceeds database precision");
        }

        inventory.setQuantityOnHand(after);
        inventoryRepository.saveAndFlush(inventory);

        StockMovement movement = new StockMovement();

        movement.setInventoryId(inventory.getId());
        movement.setMovementType(request.movementType());
        movement.setQuantityChange(change);
        movement.setQuantityBefore(before);
        movement.setQuantityAfter(after);
        movement.setReferenceNumber(request.referenceNumber());
        movement.setReason(request.reason());

        StockMovement saved = movementRepository.saveAndFlush(movement);

        return StockMovementResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public StockMovementResponse findById(Long id) {

        StockMovement movement = movementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stock movement not found"));

        return StockMovementResponse.from(movement);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> findByInventory(Long inventoryId) {

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
        if (quantity == null || quantity.signum() == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must not be zero");
        }

        if (type == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Movement type is required");
        }

        if (type != StockMovementType.ADJUSTMENT
                && quantity.signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be positive for this movement type");
        }

        return switch (type) {
            case RECEIVE, RETURN -> quantity;
            case SALE, DAMAGE -> quantity.negate();
            case ADJUSTMENT -> quantity;
        };
    }
}
