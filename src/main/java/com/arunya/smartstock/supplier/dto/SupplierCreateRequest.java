package com.arunya.smartstock.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierCreateRequest(

        @NotBlank(message = "Supplier name is required")
        @Size(max = 150)
        String name,

        @Size(max = 100)
        String contactPerson,

        @Email(message = "Invalid email address")
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 255)
        String address
) {
}