package com.arunya.smartstock.replenishment.dto;


import java.math.BigDecimal;

public record ReplenishmentResponse(

        Long inventoryId,
        Long storeId,
        Long productId,

        BigDecimal quantityOnHand,
        BigDecimal expiredQuantity,
        BigDecimal sellableQuantity,
        BigDecimal quantityReserved,
        BigDecimal quantityAvailable,

        BigDecimal incomingQuantity,
        BigDecimal projectedQuantity,

        BigDecimal reorderLevel,
        BigDecimal reorderQuantity,
        BigDecimal targetQuantity,

        BigDecimal recommendedQuantity,

        boolean lowStock,
        boolean replenishmentRequired

) {}

