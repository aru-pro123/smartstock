package com.arunya.smartstock.batch.dto;

import com.arunya.smartstock.batch.entity.BatchMovement;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BatchMovementResponse(

        Long id,
        Long batchId,
        Long stockMovementId,
        BigDecimal quantityChange,
        BigDecimal quantityBefore,
        BigDecimal quantityAfter,
        OffsetDateTime createdAt

) {

    public static BatchMovementResponse from(BatchMovement movement) {

        return new BatchMovementResponse(
                movement.getId(),
                movement.getBatchId(),
                movement.getStockMovementId(),
                movement.getQuantityChange(),
                movement.getQuantityBefore(),
                movement.getQuantityAfter(),
                movement.getCreatedAt()
        );
    }
}

