package com.arunya.smartstock.purchaseorder.repository;


import com.arunya.smartstock.purchaseorder.entity.PurchaseOrderItem;

import org.springframework.data.jpa.repository.JpaRepository;
import com.arunya.smartstock.purchaseorder.entity.PurchaseOrderStatus;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderItemRepository
        extends JpaRepository<PurchaseOrderItem, Long> {

    List<PurchaseOrderItem> findByPurchaseOrderIdOrderByIdAsc(
            Long purchaseOrderId);

    Optional<PurchaseOrderItem>
    findByPurchaseOrderIdAndProductId(
            Long purchaseOrderId,
            Long productId);

    boolean existsByPurchaseOrderIdAndProductId(
            Long purchaseOrderId,
            Long productId);

    @Query("""
    SELECT COALESCE(
        SUM(i.quantityOrdered - i.quantityReceived), 0
    )
    FROM PurchaseOrderItem i, PurchaseOrder po
    WHERE i.purchaseOrderId = po.id
      AND po.storeId = :storeId
      AND i.productId = :productId
      AND po.status IN :statuses
    """)
    BigDecimal findIncomingQuantity(
            @Param("storeId") Long storeId,
            @Param("productId") Long productId,
            @Param("statuses")
            List<PurchaseOrderStatus> statuses
    );

    @Query("""
    SELECT COUNT(i)
    FROM PurchaseOrderItem i, PurchaseOrder po
    WHERE i.purchaseOrderId = po.id
      AND po.storeId = :storeId
      AND i.productId = :productId
      AND po.status = :status
    """)
    long countByStoreAndProductAndStatus(
            @Param("storeId") Long storeId,
            @Param("productId") Long productId,
            @Param("status") PurchaseOrderStatus status
    );
}
