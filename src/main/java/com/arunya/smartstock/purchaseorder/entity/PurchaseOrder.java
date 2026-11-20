package com.arunya.smartstock.purchaseorder.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number",
            nullable = false,
            unique = true,
            length = 100)
    private String orderNumber;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseOrderStatus status =
            PurchaseOrderStatus.DRAFT;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(length = 500)
    private String notes;

    @Column(name = "ordered_at")
    private OffsetDateTime orderedAt;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

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
