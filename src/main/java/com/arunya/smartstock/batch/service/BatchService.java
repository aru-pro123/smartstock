package com.arunya.smartstock.batch.service;


import com.arunya.smartstock.batch.dto.*;
import com.arunya.smartstock.batch.repository.BatchMovementRepository;
import com.arunya.smartstock.batch.repository.InventoryBatchRepository;

import com.arunya.smartstock.inventory.repository.InventoryRepository;
import com.arunya.smartstock.store.repository.StoreRepository;

import com.arunya.smartstock.stockmovement.dto.StockMovementRequest;
import com.arunya.smartstock.stockmovement.entity.StockMovementType;
import com.arunya.smartstock.stockmovement.service.StockMovementService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class BatchService {

    private final InventoryBatchRepository batchRepository;
    private final BatchMovementRepository batchMovementRepository;
    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final StockMovementService stockMovementService;

    public BatchService(
            InventoryBatchRepository batchRepository,
            BatchMovementRepository batchMovementRepository,
            InventoryRepository inventoryRepository,
            StoreRepository storeRepository,
            StockMovementService stockMovementService
    ) {
        this.batchRepository = batchRepository;
        this.batchMovementRepository = batchMovementRepository;
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
        this.stockMovementService = stockMovementService;
    }

    @Transactional
    public BatchResponse receive(BatchReceiveRequest request) {

        stockMovementService.create(
                new StockMovementRequest(
                        request.inventoryId(),
                        StockMovementType.RECEIVE,
                        request.quantity(),
                        request.referenceNumber(),
                        request.reason(),
                        request.batchNumber(),
                        request.manufacturedDate(),
                        request.expiryDate()
                )
        );

        return batchRepository
                .findByInventoryIdAndBatchNumber(
                        request.inventoryId(),
                        request.batchNumber())
                .map(BatchResponse::from)
                .orElseThrow(() -> new IllegalStateException(
                        "Created batch could not be found"));
    }

    @Transactional(readOnly = true)
    public BatchResponse findById(Long id) {

        return batchRepository.findById(id)
                .map(BatchResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Batch not found"));
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> findByInventory(Long inventoryId) {

        if (!inventoryRepository.existsById(inventoryId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Inventory not found");
        }

        return batchRepository
                .findByInventoryIdOrderByReceivedAtDesc(inventoryId)
                .stream()
                .map(BatchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> findExpiringSoon(
            Long storeId,
            int days
    ) {

        validateStore(storeId);

        if (days < 0 || days > 365) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Days must be between 0 and 365");
        }

        LocalDate today = today();

        return batchRepository
                .findExpiringSoon(
                        storeId,
                        today,
                        today.plusDays(days))
                .stream()
                .map(BatchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BatchResponse> findExpired(Long storeId) {

        validateStore(storeId);

        return batchRepository.findExpired(storeId, today())
                .stream()
                .map(BatchResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BatchMovementResponse> findMovements(Long batchId) {

        if (!batchRepository.existsById(batchId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Batch not found");
        }

        return batchMovementRepository
                .findByBatchIdOrderByCreatedAtDescIdDesc(batchId)
                .stream()
                .map(BatchMovementResponse::from)
                .toList();
    }

    private void validateStore(Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Store not found");
        }
    }

    private LocalDate today() {
        return LocalDate.now(
                ZoneId.of("America/Vancouver"));
    }
}
