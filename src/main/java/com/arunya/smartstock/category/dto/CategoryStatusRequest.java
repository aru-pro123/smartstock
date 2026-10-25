package com.arunya.smartstock.category.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryStatusRequest(
        @NotNull Boolean active
) {}