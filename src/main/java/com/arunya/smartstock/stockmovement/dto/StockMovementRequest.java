
package com.arunya.smartstock.stockmovement.dto;

import com.arunya.smartstock.stockmovement.entity.StockMovementType;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockMovementRequest(

        @NotNull
        @Positive
        Long inventoryId,

        @NotNull
        StockMovementType movementType,

        @NotNull
        @Digits(integer = 9, fraction = 3)
        BigDecimal quantity,

        @Size(max = 100)
        String referenceNumber,

        @Size(max = 500)
        String reason,

        @Size(max = 100)
        String batchNumber,

        LocalDate manufacturedDate,

        LocalDate expiryDate

) {}
