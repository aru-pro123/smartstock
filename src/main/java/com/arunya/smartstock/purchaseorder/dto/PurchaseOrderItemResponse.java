package com.arunya.smartstock.purchaseorder.dto;


import com.arunya.smartstock.purchaseorder.entity.PurchaseOrderItem;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(

        Long id,
        Long productId,
        BigDecimal quantityOrdered,
        BigDecimal quantityReceived,
        BigDecimal quantityOutstanding,
        BigDecimal unitCost,
        BigDecimal totalCost

) {

    public static PurchaseOrderItemResponse from(
            PurchaseOrderItem item
    ) {
        return new PurchaseOrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getQuantityOrdered(),
                item.getQuantityReceived(),
                item.getQuantityOutstanding(),
                item.getUnitCost(),
                item.getTotalCost()
        );
    }
}

