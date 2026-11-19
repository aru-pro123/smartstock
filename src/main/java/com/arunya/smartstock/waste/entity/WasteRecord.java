package com.arunya.smartstock.waste.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "waste_records")
public class WasteRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "stock_movement_id",
            nullable = false,
            unique = true)
    private Long stockMovementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "waste_reason",
            nullable = false,
            length = 30)
    private WasteReason wasteReason;

    @Column(nullable = false,
            precision = 12,
            scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_cost",
            nullable = false,
            precision = 12,
            scale = 4)
    private BigDecimal unitCost;

    @Column(name = "total_loss",
            nullable = false,
            precision = 20,
            scale = 2)
    private BigDecimal totalLoss;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getStockMovementId() {
        return stockMovementId;
    }

    public void setStockMovementId(Long stockMovementId) {
        this.stockMovementId = stockMovementId;
    }

    public WasteReason getWasteReason() {
        return wasteReason;
    }

    public void setWasteReason(WasteReason wasteReason) {
        this.wasteReason = wasteReason;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalLoss() {
        return totalLoss;
    }

    public void setTotalLoss(BigDecimal totalLoss) {
        this.totalLoss = totalLoss;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

