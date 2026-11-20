package com.arunya.smartstock.replenishment.dto;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReplenishmentOrderRequest(

        @NotNull
        @Positive
        Long supplierId,

        @NotNull
        @Positive
        Long inventoryId,

        @NotNull
        @PositiveOrZero
        @Digits(integer = 8, fraction = 4)
        BigDecimal unitCost,

        LocalDate expectedDeliveryDate,

        @Size(max = 500)
        String notes

) {}

