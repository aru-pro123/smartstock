package com.arunya.smartstock.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record InventoryStatusRequest(
        @NotNull Boolean active
) {}
