package com.arunya.smartstock.purchaseorder.repository;


import com.arunya.smartstock.purchaseorder.entity.PurchaseOrderItem;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
