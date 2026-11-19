package com.arunya.smartstock.stockmovement.dto;


import com.arunya.smartstock.stockmovement.entity.StockMovement;
import com.arunya.smartstock.stockmovement.entity.StockMovementType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record StockMovementResponse(
        Long id,
        Long inventoryId,
        StockMovementType movementType,
        BigDecimal quantityChange,
        BigDecimal quantityBefore,
        BigDecimal quantityAfter,
        String referenceNumber,
        String reason,
        OffsetDateTime createdAt
) {
    public static StockMovementResponse from(StockMovement m) {
        return new StockMovementResponse(
                m.getId(),
                m.getInventoryId(),
                m.getMovementType(),
                m.getQuantityChange(),
                m.getQuantityBefore(),
                m.getQuantityAfter(),
                m.getReferenceNumber(),
                m.getReason(),
                m.getCreatedAt()
        );
    }
}
