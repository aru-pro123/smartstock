package com.arunya.smartstock.stockmovement.dto;


import com.arunya.smartstock.stockmovement.entity.StockMovementType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record StockMovementRequest(

        @NotNull
        @Positive
        Long inventoryId,

        @NotNull
        StockMovementType movementType,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 9, fraction = 3)
        BigDecimal quantity,

        @Size(max = 100)
        String referenceNumber,

        @Size(max = 500)
        String reason

) {}
