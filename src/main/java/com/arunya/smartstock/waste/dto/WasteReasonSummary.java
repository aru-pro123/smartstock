package com.arunya.smartstock.waste.dto;


import com.arunya.smartstock.waste.entity.WasteReason;

import java.math.BigDecimal;

public record WasteReasonSummary(

        WasteReason wasteReason,
        Long recordCount,
        BigDecimal totalQuantity,
        BigDecimal totalLoss

) {}

