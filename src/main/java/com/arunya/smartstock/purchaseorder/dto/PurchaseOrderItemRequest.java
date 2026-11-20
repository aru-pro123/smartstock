package com.arunya.smartstock.purchaseorder.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PurchaseOrderItemRequest(

        @NotNull
        @Positive
        Long productId,

        @NotNull
        @Positive
        @Digits(integer = 9, fraction = 3)
        BigDecimal quantity,

        @NotNull
        @PositiveOrZero
        @Digits(integer = 8, fraction = 4)
        BigDecimal unitCost

) {}
