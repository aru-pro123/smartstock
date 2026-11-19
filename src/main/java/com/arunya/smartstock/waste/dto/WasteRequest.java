package com.arunya.smartstock.waste.dto;


import com.arunya.smartstock.waste.entity.WasteReason;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record WasteRequest(

        @NotNull
        @Positive
        Long batchId,

        @NotNull
        WasteReason wasteReason,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 9, fraction = 3)
        BigDecimal quantity,

        @NotNull
        @DecimalMin("0")
        @Digits(integer = 8, fraction = 4)
        BigDecimal unitCost,

        @Size(max = 500)
        String notes

) {}
