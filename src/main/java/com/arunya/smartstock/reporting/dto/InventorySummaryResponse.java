package com.arunya.smartstock.reporting.dto;


import java.math.BigDecimal;

public record InventorySummaryResponse(
        Long storeId,
        long inventoryRecords,
        long activeInventoryRecords,
        long lowStockProducts,
        BigDecimal quantityOnHand,
        BigDecimal quantityReserved,
        BigDecimal sellableQuantity,
        BigDecimal availableQuantity,
        BigDecimal expiredQuantity
) {}
