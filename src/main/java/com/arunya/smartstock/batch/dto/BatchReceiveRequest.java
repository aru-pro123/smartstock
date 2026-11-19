package com.arunya.smartstock.batch.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BatchReceiveRequest(

        @NotNull
        @Positive
        Long inventoryId,

        @NotBlank
        @Size(max = 100)
        String batchNumber,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 9, fraction = 3)
        BigDecimal quantity,

        LocalDate manufacturedDate,

        LocalDate expiryDate,

        @Size(max = 100)
        String referenceNumber,

        @Size(max = 500)
        String reason

) {}

