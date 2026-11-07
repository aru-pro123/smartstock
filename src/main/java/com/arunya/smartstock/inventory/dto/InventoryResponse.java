package com.arunya.smartstock.inventory.dto;


import com.arunya.smartstock.inventory.entity.StoreInventory;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record InventoryResponse(
        Long id,
        Long storeId,
        Long productId,
        BigDecimal quantityOnHand,
        BigDecimal quantityReserved,
        BigDecimal quantityAvailable,
        BigDecimal reorderLevel,
        BigDecimal reorderQuantity,
        boolean lowStock,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static InventoryResponse from(StoreInventory i) {

        BigDecimal available =
                i.getQuantityOnHand()
                        .subtract(i.getQuantityReserved());

        return new InventoryResponse(
                i.getId(),
                i.getStoreId(),
                i.getProductId(),
                i.getQuantityOnHand(),
                i.getQuantityReserved(),
                available,
                i.getReorderLevel(),
                i.getReorderQuantity(),
                i.getActive()
                        && available.compareTo(i.getReorderLevel()) <= 0,
                i.getActive(),
                i.getCreatedAt(),
                i.getUpdatedAt()
        );
    }
}
