package com.arunya.smartstock.waste.dto;


import com.arunya.smartstock.waste.entity.WasteReason;
import com.arunya.smartstock.waste.entity.WasteRecord;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WasteResponse(

        Long id,
        Long inventoryId,
        Long batchId,
        Long stockMovementId,
        WasteReason wasteReason,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalLoss,
        String notes,
        OffsetDateTime createdAt

) {

    public static WasteResponse from(WasteRecord waste) {

        return new WasteResponse(
                waste.getId(),
                waste.getInventoryId(),
                waste.getBatchId(),
                waste.getStockMovementId(),
                waste.getWasteReason(),
                waste.getQuantity(),
                waste.getUnitCost(),
                waste.getTotalLoss(),
                waste.getNotes(),
                waste.getCreatedAt()
        );
    }
}

