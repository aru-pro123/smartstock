package com.arunya.smartstock.batch.service;


import com.arunya.smartstock.batch.entity.BatchMovement;
import com.arunya.smartstock.batch.entity.InventoryBatch;
import com.arunya.smartstock.batch.repository.BatchMovementRepository;
import com.arunya.smartstock.batch.repository.InventoryBatchRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class BatchAllocationService {

    private final InventoryBatchRepository batchRepository;
    private final BatchMovementRepository batchMovementRepository;

    public BatchAllocationService(
            InventoryBatchRepository batchRepository,
            BatchMovementRepository batchMovementRepository
    ) {
        this.batchRepository = batchRepository;
        this.batchMovementRepository = batchMovementRepository;
    }

    public InventoryBatch receive(
            Long inventoryId,
            String batchNumber,
            BigDecimal quantity,
            LocalDate manufacturedDate,
            LocalDate expiryDate,
            Long stockMovementId,
            LocalDate today
    ) {

        if (batchNumber == null || batchNumber.isBlank()) {
            throw error("Batch number is required");
        }

        if (batchNumber.length() > 100) {
            throw error("Batch number is too long");
        }

        if (batchRepository.existsByInventoryIdAndBatchNumber(
                inventoryId, batchNumber)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Batch number already exists");
        }

        if (manufacturedDate != null
                && manufacturedDate.isAfter(today)) {
            throw error("Manufactured date cannot be in the future");
        }

        if (expiryDate != null && expiryDate.isBefore(today)) {
            throw error("Cannot receive expired stock");
        }

        if (manufacturedDate != null
                && expiryDate != null
                && expiryDate.isBefore(manufacturedDate)) {

            throw error("Expiry date cannot precede manufactured date");
        }

        InventoryBatch batch = new InventoryBatch();

        batch.setInventoryId(inventoryId);
        batch.setBatchNumber(batchNumber);
        batch.setQuantityReceived(quantity);
        batch.setQuantityRemaining(quantity);
        batch.setManufacturedDate(manufacturedDate);
        batch.setExpiryDate(expiryDate);

        InventoryBatch saved = batchRepository.saveAndFlush(batch);

        recordMovement(
                saved.getId(),
                stockMovementId,
                quantity,
                BigDecimal.ZERO,
                quantity
        );

        return saved;
    }

    public void allocateFEFO(
            Long inventoryId,
            BigDecimal quantity,
            Long stockMovementId,
            LocalDate today
    ) {

        List<InventoryBatch> batches =
                batchRepository.findAvailableBatches(
                        inventoryId, today);

        BigDecimal remaining = quantity;

        for (InventoryBatch batch : batches) {

            if (remaining.signum() == 0) {
                break;
            }

            BigDecimal before = batch.getQuantityRemaining();
            BigDecimal allocated = before.min(remaining);
            BigDecimal after = before.subtract(allocated);

            batch.setQuantityRemaining(after);
            batchRepository.save(batch);

            recordMovement(
                    batch.getId(),
                    stockMovementId,
                    allocated.negate(),
                    before,
                    after
            );

            remaining = remaining.subtract(allocated);
        }

        if (remaining.signum() > 0) {
            throw error("Insufficient non-expired batch stock");
        }
    }

    public void deductFromBatch(
            Long inventoryId,
            String batchNumber,
            BigDecimal quantity,
            Long stockMovementId
    ) {

        if (batchNumber == null || batchNumber.isBlank()) {
            throw error("Batch number is required");
        }

        InventoryBatch batch = batchRepository
                .findByInventoryIdAndBatchNumber(
                        inventoryId, batchNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Batch not found"));

        BigDecimal before = batch.getQuantityRemaining();
        BigDecimal after = before.subtract(quantity);

        if (after.signum() < 0) {
            throw error("Insufficient quantity in selected batch");
        }

        batch.setQuantityRemaining(after);
        batchRepository.save(batch);

        recordMovement(
                batch.getId(),
                stockMovementId,
                quantity.negate(),
                before,
                after
        );
    }

    private void recordMovement(
            Long batchId,
            Long stockMovementId,
            BigDecimal change,
            BigDecimal before,
            BigDecimal after
    ) {

        BatchMovement movement = new BatchMovement();

        movement.setBatchId(batchId);
        movement.setStockMovementId(stockMovementId);
        movement.setQuantityChange(change);
        movement.setQuantityBefore(before);
        movement.setQuantityAfter(after);

        batchMovementRepository.save(movement);
    }

    private ResponseStatusException error(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }
}
