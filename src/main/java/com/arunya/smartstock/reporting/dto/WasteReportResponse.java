package com.arunya.smartstock.reporting.dto;

import com.arunya.smartstock.waste.dto.WasteReasonSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WasteReportResponse(
        Long storeId,
        LocalDate from,
        LocalDate to,
        String currency,
        long totalRecords,
        BigDecimal totalQuantity,
        BigDecimal totalLoss,
        List<WasteReasonSummary> breakdown
) {}

