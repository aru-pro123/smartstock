package com.arunya.smartstock.purchaseorder.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "purchase_order_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_purchase_order_product",
                        columnNames = {
                                "purchase_order_id",
                                "product_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_order_id", nullable = false)
    private Long purchaseOrderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity_ordered",
            nullable = false,
            precision = 12,
            scale = 3)
    private BigDecimal quantityOrdered;

    @Column(name = "quantity_received",
            nullable = false,
            precision = 12,
            scale = 3)
    private BigDecimal quantityReceived = BigDecimal.ZERO;

    @Column(name = "unit_cost",
            nullable = false,
            precision = 12,
            scale = 4)
    private BigDecimal unitCost;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public BigDecimal getQuantityOutstanding() {
        return quantityOrdered.subtract(quantityReceived);
    }

    public BigDecimal getTotalCost() {
        return quantityOrdered.multiply(unitCost);
    }

    public boolean isFullyReceived() {
        return quantityReceived.compareTo(quantityOrdered) == 0;
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
