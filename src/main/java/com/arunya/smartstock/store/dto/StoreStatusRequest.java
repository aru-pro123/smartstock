package com.arunya.smartstock.store.dto;

import jakarta.validation.constraints.NotNull;

public record StoreStatusRequest(
        @NotNull Boolean active
) {}