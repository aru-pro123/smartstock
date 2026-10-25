package com.arunya.smartstock.product.dto;

import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(
        @NotNull Boolean active
) {}