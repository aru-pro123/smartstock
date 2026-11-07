package com.arunya.smartstock.inventory.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record InventoryReservationRequest(

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 9, fraction = 3)
        BigDecimal quantity,

        @NotNull
        ReservationType type

) {
    public enum ReservationType {
        RESERVE,
        RELEASE
    }
}
