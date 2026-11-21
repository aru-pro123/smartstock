package com.arunya.smartstock.reporting.controller;

import com.arunya.smartstock.reporting.dto.*;
import com.arunya.smartstock.reporting.service.ReportingService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(
            ReportingService reportingService
    ) {
        this.reportingService = reportingService;
    }

    @GetMapping("/stores/{storeId}/inventory")
    public InventorySummaryResponse inventorySummary(
            @PathVariable Long storeId
    ) {
        return reportingService.inventorySummary(storeId);
    }

    @GetMapping("/stores/{storeId}/expiry")
    public ExpirySummaryResponse expirySummary(
            @PathVariable Long storeId
    ) {
        return reportingService.expirySummary(storeId);
    }

    @GetMapping("/stores/{storeId}/stock-movements")
    public StockMovementSummaryResponse movementSummary(
            @PathVariable Long storeId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return reportingService.movementSummary(
                storeId, from, to
        );
    }

    @GetMapping("/stores/{storeId}/waste")
    public WasteReportResponse wasteSummary(
            @PathVariable Long storeId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return reportingService.wasteSummary(
                storeId, from, to
        );
    }

    @GetMapping("/stores/{storeId}/purchase-orders")
    public PurchaseOrderSummaryResponse purchaseOrderSummary(
            @PathVariable Long storeId
    ) {
        return reportingService.purchaseOrderSummary(storeId);
    }

    @GetMapping("/stores/{storeId}/dashboard")
    public DashboardResponse dashboard(
            @PathVariable Long storeId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return reportingService.dashboard(
                storeId, from, to
        );
    }
}

