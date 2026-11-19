package com.arunya.smartstock.batch.dto;


import com.arunya.smartstock.batch.entity.InventoryBatch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public record BatchResponse(

        Long id,
        Long inventoryId,
        String batchNumber,
        BigDecimal quantityReceived,
        BigDecimal quantityRemaining,
        LocalDate manufacturedDate,
        LocalDate expiryDate,
        Long daysUntilExpiry,
        BatchStatus status,
        OffsetDateTime receivedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt

) {

    public static BatchResponse from(InventoryBatch batch) {

        LocalDate today = LocalDate.now(
                ZoneId.of("America/Vancouver"));

        Long daysUntilExpiry = batch.getExpiryDate() == null
                ? null
                : ChronoUnit.DAYS.between(
                today,
                batch.getExpiryDate()
        );

        return new BatchResponse(
                batch.getId(),
                batch.getInventoryId(),
                batch.getBatchNumber(),
                batch.getQuantityReceived(),
                batch.getQuantityRemaining(),
                batch.getManufacturedDate(),
                batch.getExpiryDate(),
                daysUntilExpiry,
                calculateStatus(batch, daysUntilExpiry),
                batch.getReceivedAt(),
                batch.getCreatedAt(),
                batch.getUpdatedAt()
        );
    }

    private static BatchStatus calculateStatus(
            InventoryBatch batch,
            Long daysUntilExpiry
    ) {

        if (batch.getQuantityRemaining().signum() == 0) {
            return BatchStatus.DEPLETED;
        }

        if (daysUntilExpiry == null) {
            return BatchStatus.NO_EXPIRY;
        }

        if (daysUntilExpiry < 0) {
            return BatchStatus.EXPIRED;
        }

        if (daysUntilExpiry <= 7) {
            return BatchStatus.EXPIRING_SOON;
        }

        return BatchStatus.AVAILABLE;
    }
}

