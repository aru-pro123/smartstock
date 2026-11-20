package com.arunya.smartstock.purchaseorder.repository;

import com.arunya.smartstock.purchaseorder.entity.PurchaseOrder;
import com.arunya.smartstock.purchaseorder.entity.PurchaseOrderStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    boolean existsByOrderNumber(String orderNumber);

    Optional<PurchaseOrder> findByOrderNumber(
            String orderNumber);

    List<PurchaseOrder> findAllByOrderByCreatedAtDescIdDesc();

    List<PurchaseOrder> findByStoreIdOrderByCreatedAtDescIdDesc(
            Long storeId);

    List<PurchaseOrder> findBySupplierIdOrderByCreatedAtDescIdDesc(
            Long supplierId);

    List<PurchaseOrder> findByStoreIdAndStatusIn(
            Long storeId,
            List<PurchaseOrderStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT po FROM PurchaseOrder po
        WHERE po.id = :id
        """)
    Optional<PurchaseOrder> findByIdForUpdate(
            @Param("id") Long id);
}
