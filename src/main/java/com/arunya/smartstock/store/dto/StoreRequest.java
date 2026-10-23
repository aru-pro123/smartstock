package com.arunya.smartstock.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreRequest(
        @NotBlank @Size(max = 20) String storeCode,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 50) String province,
        @Size(max = 255) String address
) {}