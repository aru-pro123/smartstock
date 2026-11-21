package com.arunya.smartstock.reporting.dto;

import com.arunya.smartstock.stockmovement.entity.StockMovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StockMovementSummaryResponse(
        Long storeId,
        LocalDate from,
        LocalDate to,
        long totalMovements,
        List<MovementBreakdown> breakdown
) {
    public record MovementBreakdown(
            StockMovementType movementType,
            long count,
            BigDecimal totalQuantityChange
    ) {}
}
