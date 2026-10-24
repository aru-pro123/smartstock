package com.arunya.smartstock.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank String sku,

        String barcode,

        @NotBlank String name,

        String brand,

        @NotNull Long categoryId,

        @NotBlank String unitOfMeasure,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal sellingPrice,

        boolean taxable,

        boolean perishable,

        @Positive Integer shelfLifeDays

) {}