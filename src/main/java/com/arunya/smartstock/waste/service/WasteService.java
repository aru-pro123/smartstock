package com.arunya.smartstock.waste.service;


import com.arunya.smartstock.batch.entity.InventoryBatch;
import com.arunya.smartstock.batch.repository.InventoryBatchRepository;

import com.arunya.smartstock.inventory.repository.InventoryRepository;

import com.arunya.smartstock.stockmovement.dto.StockMovementRequest;
import com.arunya.smartstock.stockmovement.dto.StockMovementResponse;
import com.arunya.smartstock.stockmovement.entity.StockMovementType;
import com.arunya.smartstock.stockmovement.service.StockMovementService;

import com.arunya.smartstock.store.repository.StoreRepository;

import com.arunya.smartstock.waste.dto.*;
import com.arunya.smartstock.waste.entity.WasteReason;
import com.arunya.smartstock.waste.entity.WasteRecord;
import com.arunya.smartstock.waste.repository.WasteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class WasteService {

    private final WasteRepository wasteRepository;
    private final InventoryBatchRepository batchRepository;
    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final StockMovementService stockMovementService;

    public WasteService(
            WasteRepository wasteRepository,
            InventoryBatchRepository batchRepository,
            InventoryRepository inventoryRepository,
            StoreRepository storeRepository,
            StockMovementService stockMovementService
    ) {
        this.wasteRepository = wasteRepository;
        this.batchRepository = batchRepository;
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
        this.stockMovementService = stockMovementService;
    }

    @Transactional
    public WasteResponse create(WasteRequest request) {

        InventoryBatch batch = batchRepository
                .findById(request.batchId())
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Batch not found"));

        Long inventoryId = batch.getInventoryId();

        inventoryRepository.findByIdForUpdate(inventoryId)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Inventory not found"));

        validateRequest(request);
        validateWasteReason(batch, request);

        BigDecimal totalLoss = request.quantity()
                .multiply(request.unitCost())
                .setScale(2, RoundingMode.HALF_UP);

        StockMovementRequest movementRequest =
                new StockMovementRequest(
                        inventoryId,
                        StockMovementType.DAMAGE,
                        request.quantity(),
                        null,
                        "Waste: " + request.wasteReason().name(),
                        batch.getBatchNumber(),
                        null,
                        null
                );

        StockMovementResponse movement =
                stockMovementService.create(movementRequest);

        WasteRecord waste = new WasteRecord();

        waste.setInventoryId(inventoryId);
        waste.setBatchId(batch.getId());
        waste.setStockMovementId(movement.id());
        waste.setWasteReason(request.wasteReason());
        waste.setQuantity(request.quantity());
        waste.setUnitCost(request.unitCost());
        waste.setTotalLoss(totalLoss);
        waste.setNotes(request.notes());

        return WasteResponse.from(
                wasteRepository.saveAndFlush(waste));
    }

    @Transactional(readOnly = true)
    public WasteResponse findById(Long id) {

        return wasteRepository.findById(id)
                .map(WasteResponse::from)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Waste record not found"));
    }

    @Transactional(readOnly = true)
    public List<WasteResponse> findByInventory(
            Long inventoryId
    ) {

        if (!inventoryRepository.existsById(inventoryId)) {
            throw error(
                    HttpStatus.NOT_FOUND,
                    "Inventory not found");
        }

        return wasteRepository
                .findByInventoryIdOrderByCreatedAtDescIdDesc(
                        inventoryId)
                .stream()
                .map(WasteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WasteResponse> findByBatch(Long batchId) {

        if (!batchRepository.existsById(batchId)) {
            throw error(
                    HttpStatus.NOT_FOUND,
                    "Batch not found");
        }

        return wasteRepository
                .findByBatchIdOrderByCreatedAtDescIdDesc(batchId)
                .stream()
                .map(WasteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WasteResponse> findByStore(Long storeId) {

        validateStore(storeId);

        return wasteRepository.findByStoreId(storeId)
                .stream()
                .map(WasteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WasteSummaryResponse getSummary(Long storeId) {

        validateStore(storeId);

        List<WasteReasonSummary> breakdown =
                wasteRepository.summarizeByStoreId(storeId);

        long totalRecords = breakdown.stream()
                .mapToLong(WasteReasonSummary::recordCount)
                .sum();

        BigDecimal totalQuantity = breakdown.stream()
                .map(WasteReasonSummary::totalQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLoss = breakdown.stream()
                .map(WasteReasonSummary::totalLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new WasteSummaryResponse(
                storeId,
                "CAD",
                totalRecords,
                totalQuantity,
                totalLoss.setScale(2, RoundingMode.HALF_UP),
                breakdown
        );
    }

    private void validateRequest(WasteRequest request) {

        if (request.wasteReason() == null) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Waste reason is required");
        }

        if (request.quantity() == null
                || request.quantity().signum() <= 0) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Waste quantity must be positive");
        }

        if (request.unitCost() == null
                || request.unitCost().signum() < 0) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Unit cost cannot be negative");
        }

        if (request.quantity().scale() > 3
                || request.quantity().precision()
                - request.quantity().scale() > 9) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Invalid quantity precision");
        }

        if (request.unitCost().scale() > 4
                || request.unitCost().precision()
                - request.unitCost().scale() > 8) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Invalid unit cost precision");
        }

        if (request.notes() != null
                && request.notes().length() > 500) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Notes exceed maximum length");
        }

        if (request.wasteReason() == WasteReason.OTHER
                && (request.notes() == null
                || request.notes().isBlank())) {

            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Notes are required for OTHER waste reason");
        }
    }

    private void validateWasteReason(
            InventoryBatch batch,
            WasteRequest request
    ) {

        LocalDate today = LocalDate.now(
                ZoneId.of("America/Vancouver"));

        if (request.wasteReason() == WasteReason.EXPIRED) {

            if (batch.getExpiryDate() == null) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "Batch has no recorded expiry date");
            }

            if (!batch.getExpiryDate().isBefore(today)) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "Batch has not expired");
            }
        }
    }

    private void validateStore(Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw error(
                    HttpStatus.NOT_FOUND,
                    "Store not found");
        }
    }

    private ResponseStatusException error(
            HttpStatus status,
            String message
    ) {
        return new ResponseStatusException(status, message);
    }
}

