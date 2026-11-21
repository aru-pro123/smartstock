package com.arunya.smartstock.reporting.dto;

public record DashboardResponse(
        Long storeId,
        InventorySummaryResponse inventory,
        ExpirySummaryResponse expiry,
        WasteReportResponse waste,
        PurchaseOrderSummaryResponse purchaseOrders
) {}
