package com.arunya.smartstock.purchaseorder.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderCreateRequest(

        @NotNull
        @Positive
        Long supplierId,

        @NotNull
        @Positive
        Long storeId,

        LocalDate expectedDeliveryDate,

        @Size(max = 500)
        String notes,

        @NotEmpty
        List<@NotNull @Valid PurchaseOrderItemRequest> items

) {}
