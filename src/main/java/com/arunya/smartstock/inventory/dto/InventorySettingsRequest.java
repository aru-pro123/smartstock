package com.arunya.smartstock.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record InventorySettingsRequest(

        @NotNull
        @DecimalMin("0.0")
        @Digits(integer = 9, fraction = 3)
        BigDecimal reorderLevel,

        @NotNull
        @DecimalMin("0.0")
        @Digits(integer = 9, fraction = 3)
        BigDecimal reorderQuantity

) {}
