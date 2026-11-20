package com.arunya.smartstock.purchaseorder.dto;


import com.arunya.smartstock.purchaseorder.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record PurchaseOrderResponse(

        Long id,
        String orderNumber,
        Long supplierId,
        Long storeId,
        PurchaseOrderStatus status,
        LocalDate expectedDeliveryDate,
        String notes,
        BigDecimal totalCost,
        OffsetDateTime orderedAt,
        OffsetDateTime receivedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<PurchaseOrderItemResponse> items

) {

    public static PurchaseOrderResponse from(
            PurchaseOrder order,
            List<PurchaseOrderItem> items
    ) {

        BigDecimal totalCost = items.stream()
                .map(PurchaseOrderItem::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PurchaseOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getSupplierId(),
                order.getStoreId(),
                order.getStatus(),
                order.getExpectedDeliveryDate(),
                order.getNotes(),
                totalCost,
                order.getOrderedAt(),
                order.getReceivedAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items.stream()
                        .map(PurchaseOrderItemResponse::from)
                        .toList()
        );
    }
}
