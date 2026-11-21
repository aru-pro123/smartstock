package com.arunya.smartstock.reporting.dto;

import com.arunya.smartstock.purchaseorder.entity.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseOrderSummaryResponse(
        Long storeId,
        long totalOrders,
        long pendingOrders,
        BigDecimal outstandingQuantity,
        BigDecimal outstandingValue,
        List<StatusBreakdown> breakdown
) {
    public record StatusBreakdown(
            PurchaseOrderStatus status,
            long count
    ) {}
}

