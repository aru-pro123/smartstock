package com.arunya.smartstock.purchaseorder.dto;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseOrderReceiveItemRequest(

        @NotNull
        @Positive
        Long productId,

        @NotNull
        @Positive
        @Digits(integer = 9, fraction = 3)
        BigDecimal quantity,

        @NotBlank
        @Size(max = 100)
        String batchNumber,

        LocalDate manufacturedDate,

        LocalDate expiryDate

) {}
