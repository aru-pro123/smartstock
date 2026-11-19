package com.arunya.smartstock.waste.dto;


import java.math.BigDecimal;
import java.util.List;

public record WasteSummaryResponse(

        Long storeId,
        String currency,
        Long totalRecords,
        BigDecimal totalQuantity,
        BigDecimal totalLoss,
        List<WasteReasonSummary> breakdown

) {}
