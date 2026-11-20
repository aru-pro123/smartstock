package com.arunya.smartstock.purchaseorder.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record PurchaseOrderReceiveRequest(

        @NotEmpty
        List<@NotNull @Valid PurchaseOrderReceiveItemRequest> items

) {}

