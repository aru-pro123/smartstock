package com.arunya.smartstock.reporting.service;


import com.arunya.smartstock.reporting.dto.*;

import com.arunya.smartstock.inventory.entity.StoreInventory;
import com.arunya.smartstock.inventory.repository.InventoryRepository;

import com.arunya.smartstock.batch.entity.InventoryBatch;
import com.arunya.smartstock.batch.repository.InventoryBatchRepository;

import com.arunya.smartstock.stockmovement.entity.StockMovementType;
import com.arunya.smartstock.stockmovement.repository.StockMovementRepository;

import com.arunya.smartstock.waste.dto.WasteReasonSummary;
import com.arunya.smartstock.waste.entity.WasteReason;
import com.arunya.smartstock.waste.repository.WasteRepository;

import com.arunya.smartstock.purchaseorder.entity.PurchaseOrder;
import com.arunya.smartstock.purchaseorder.entity.PurchaseOrderStatus;
import com.arunya.smartstock.purchaseorder.repository.PurchaseOrderRepository;
import com.arunya.smartstock.purchaseorder.repository.PurchaseOrderItemRepository;

import com.arunya.smartstock.store.repository.StoreRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportingService {

    private static final ZoneId STORE_ZONE =
            ZoneId.of("America/Vancouver");

    private static final int EXPIRY_WINDOW_DAYS = 7;

    private static final List<PurchaseOrderStatus> PENDING_STATUSES =
            List.of(
                    PurchaseOrderStatus.ORDERED,
                    PurchaseOrderStatus.PARTIALLY_RECEIVED
            );

    private final InventoryRepository inventoryRepository;
    private final InventoryBatchRepository batchRepository;
    private final StockMovementRepository movementRepository;
    private final WasteRepository wasteRepository;
    private final PurchaseOrderRepository orderRepository;
    private final PurchaseOrderItemRepository itemRepository;
    private final StoreRepository storeRepository;

    public ReportingService(
            InventoryRepository inventoryRepository,
            InventoryBatchRepository batchRepository,
            StockMovementRepository movementRepository,
            WasteRepository wasteRepository,
            PurchaseOrderRepository orderRepository,
            PurchaseOrderItemRepository itemRepository,
            StoreRepository storeRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.batchRepository = batchRepository;
        this.movementRepository = movementRepository;
        this.wasteRepository = wasteRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
    }

    public InventorySummaryResponse inventorySummary(Long storeId) {
        validateStore(storeId);

        LocalDate today = LocalDate.now(STORE_ZONE);

        List<StoreInventory> inventories =
                inventoryRepository.findByStoreId(storeId);

        long activeCount = 0;
        long lowStockCount = 0;

        BigDecimal physical = BigDecimal.ZERO;
        BigDecimal reserved = BigDecimal.ZERO;
        BigDecimal sellable = BigDecimal.ZERO;
        BigDecimal available = BigDecimal.ZERO;
        BigDecimal expired = BigDecimal.ZERO;

        for (StoreInventory inventory : inventories) {

            if (!Boolean.TRUE.equals(inventory.getActive())) {
                continue;
            }

            activeCount++;

            BigDecimal onHand = inventory.getQuantityOnHand();
            BigDecimal stockReserved = inventory.getQuantityReserved();

            BigDecimal sellableStock =
                    batchRepository.findSellableQuantity(
                            inventory.getId(), today);

            BigDecimal totalBatchQuantity =
                    batchRepository.findTotalQuantity(
                            inventory.getId());

            if (totalBatchQuantity.compareTo(onHand) != 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Inventory and batch quantities are inconsistent: "
                                + inventory.getId()
                );
            }

            BigDecimal availableStock =
                    sellableStock.subtract(stockReserved)
                            .max(BigDecimal.ZERO);

            physical = physical.add(onHand);
            reserved = reserved.add(stockReserved);
            sellable = sellable.add(sellableStock);
            available = available.add(availableStock);
            expired = expired.add(
                    onHand.subtract(sellableStock));

            if (availableStock.compareTo(
                    inventory.getReorderLevel()) <= 0) {
                lowStockCount++;
            }
        }

        return new InventorySummaryResponse(
                storeId,
                inventories.size(),
                activeCount,
                lowStockCount,
                physical,
                reserved,
                sellable,
                available,
                expired
        );
    }

    public ExpirySummaryResponse expirySummary(Long storeId) {
        validateStore(storeId);

        LocalDate today = LocalDate.now(STORE_ZONE);
        LocalDate endDate = today.plusDays(EXPIRY_WINDOW_DAYS);

        List<InventoryBatch> expired =
                batchRepository.findExpired(storeId, today);

        List<InventoryBatch> expiringSoon =
                batchRepository.findExpiringSoon(
                        storeId, today, endDate);

        return new ExpirySummaryResponse(
                storeId,
                today,
                endDate,
                expired.size(),
                sumBatchQuantities(expired),
                expiringSoon.size(),
                sumBatchQuantities(expiringSoon)
        );
    }

    public StockMovementSummaryResponse movementSummary(
            Long storeId,
            LocalDate from,
            LocalDate to
    ) {
        validateStore(storeId);

        DateRange range = resolveRange(from, to);

        List<Object[]> rows =
                movementRepository.summarizeMovements(
                        storeId,
                        range.start(),
                        range.endExclusive()
                );

        List<StockMovementSummaryResponse.MovementBreakdown> breakdown =
                new ArrayList<>();

        long totalCount = 0;

        for (Object[] row : rows) {

            StockMovementType type =
                    (StockMovementType) row[0];

            long count = ((Number) row[1]).longValue();
            BigDecimal quantity = decimal(row[2]);

            totalCount += count;

            breakdown.add(
                    new StockMovementSummaryResponse.MovementBreakdown(
                            type,
                            count,
                            quantity
                    )
            );
        }

        breakdown.sort(Comparator.comparing(
                item -> item.movementType().name()
        ));

        return new StockMovementSummaryResponse(
                storeId,
                range.from(),
                range.to(),
                totalCount,
                breakdown
        );
    }

    public WasteReportResponse wasteSummary(
            Long storeId,
            LocalDate from,
            LocalDate to
    ) {
        validateStore(storeId);

        DateRange range = resolveRange(from, to);

        List<Object[]> rows =
                wasteRepository.summarizeWasteInPeriod(
                        storeId,
                        range.start(),
                        range.endExclusive()
                );

        long totalRecords = 0;
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;

        List<WasteReasonSummary> breakdown = new ArrayList<>();

        for (Object[] row : rows) {

            WasteReason reason = (WasteReason) row[0];
            long count = ((Number) row[1]).longValue();
            BigDecimal quantity = decimal(row[2]);
            BigDecimal loss = decimal(row[3]);

            totalRecords += count;
            totalQuantity = totalQuantity.add(quantity);
            totalLoss = totalLoss.add(loss);

            breakdown.add(new WasteReasonSummary(
                    reason,
                    count,
                    quantity,
                    loss
            ));
        }

        breakdown.sort(
                Comparator.comparing(
                        WasteReasonSummary::totalLoss
                ).reversed()
        );

        return new WasteReportResponse(
                storeId,
                range.from(),
                range.to(),
                "CAD",
                totalRecords,
                totalQuantity,
                totalLoss,
                breakdown
        );
    }

    public PurchaseOrderSummaryResponse purchaseOrderSummary(
            Long storeId
    ) {
        validateStore(storeId);

        List<PurchaseOrder> orders =
                orderRepository
                        .findByStoreIdOrderByCreatedAtDescIdDesc(storeId);

        Map<PurchaseOrderStatus, Long> counts =
                orders.stream()
                        .collect(Collectors.groupingBy(
                                PurchaseOrder::getStatus,
                                () -> new EnumMap<>(PurchaseOrderStatus.class),
                                Collectors.counting()
                        ));

        List<PurchaseOrderSummaryResponse.StatusBreakdown> breakdown =
                Arrays.stream(PurchaseOrderStatus.values())
                        .map(status ->
                                new PurchaseOrderSummaryResponse.StatusBreakdown(
                                        status,
                                        counts.getOrDefault(status, 0L)
                                )
                        )
                        .toList();

        long pendingCount =
                counts.getOrDefault(
                        PurchaseOrderStatus.ORDERED, 0L)
                        + counts.getOrDefault(
                        PurchaseOrderStatus.PARTIALLY_RECEIVED, 0L);

        // Single-row aggregate query returns outstanding
        // quantity and outstanding purchasing value.

        Object[] result = itemRepository.summarizeOutstandingOrders(
                storeId,
                PENDING_STATUSES
        );

// Spring Data JPA may wrap the aggregate row
// inside an additional Object[].
        Object[] totals = result.length == 1
                && result[0] instanceof Object[]
                ? (Object[]) result[0]
                : result;

        BigDecimal outstandingQuantity = decimal(totals[0]);
        BigDecimal outstandingValue = decimal(totals[1]);


        return new PurchaseOrderSummaryResponse(
                storeId,
                orders.size(),
                pendingCount,
                outstandingQuantity,
                outstandingValue,
                breakdown
        );
    }

    public DashboardResponse dashboard(
            Long storeId,
            LocalDate from,
            LocalDate to
    ) {
        validateStore(storeId);

        return new DashboardResponse(
                storeId,
                inventorySummary(storeId),
                expirySummary(storeId),
                wasteSummary(storeId, from, to),
                purchaseOrderSummary(storeId)
        );
    }

    private BigDecimal sumBatchQuantities(
            List<InventoryBatch> batches
    ) {
        return batches.stream()
                .map(InventoryBatch::getQuantityRemaining)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal decimal(Object value) {
        return value == null
                ? BigDecimal.ZERO
                : (BigDecimal) value;
    }

    private DateRange resolveRange(
            LocalDate from,
            LocalDate to
    ) {
        LocalDate today = LocalDate.now(STORE_ZONE);

        // Default: current calendar month through today.
        LocalDate start = from != null
                ? from
                : today.withDayOfMonth(1);

        LocalDate end = to != null
                ? to
                : today;

        if (start.isAfter(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "'from' must be on or before 'to'"
            );
        }

        return new DateRange(
                start,
                end,
                start.atStartOfDay(STORE_ZONE).toOffsetDateTime(),
                end.plusDays(1)
                        .atStartOfDay(STORE_ZONE)
                        .toOffsetDateTime()
        );
    }

    private void validateStore(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Store not found: " + storeId
            );
        }
    }

    private record DateRange(
            LocalDate from,
            LocalDate to,
            OffsetDateTime start,
            OffsetDateTime endExclusive
    ) {}
}
