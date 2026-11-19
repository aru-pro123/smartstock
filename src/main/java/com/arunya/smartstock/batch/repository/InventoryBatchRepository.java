package com.arunya.smartstock.batch.repository;

import com.arunya.smartstock.batch.entity.InventoryBatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryBatchRepository
        extends JpaRepository<InventoryBatch, Long> {

    boolean existsByInventoryIdAndBatchNumber(
            Long inventoryId,
            String batchNumber
    );

    Optional<InventoryBatch> findByInventoryIdAndBatchNumber(
            Long inventoryId,
            String batchNumber
    );

    List<InventoryBatch> findByInventoryIdOrderByReceivedAtDesc(
            Long inventoryId
    );

    @Query("""
        SELECT b FROM InventoryBatch b
        WHERE b.inventoryId = :inventoryId
          AND b.quantityRemaining > 0
          AND (
              b.expiryDate IS NULL
              OR b.expiryDate >= :today
          )
        ORDER BY
          CASE WHEN b.expiryDate IS NULL THEN 1 ELSE 0 END,
          b.expiryDate ASC,
          b.receivedAt ASC,
          b.id ASC
        """)
    List<InventoryBatch> findAvailableBatches(
            @Param("inventoryId") Long inventoryId,
            @Param("today") LocalDate today
    );

    @Query("""
        SELECT COALESCE(SUM(b.quantityRemaining), 0)
        FROM InventoryBatch b
        WHERE b.inventoryId = :inventoryId
          AND (
              b.expiryDate IS NULL
              OR b.expiryDate >= :today
          )
        """)
    BigDecimal findSellableQuantity(
            @Param("inventoryId") Long inventoryId,
            @Param("today") LocalDate today
    );

    @Query("""
        SELECT b FROM InventoryBatch b, StoreInventory i
        WHERE b.inventoryId = i.id
          AND i.storeId = :storeId
          AND b.quantityRemaining > 0
          AND b.expiryDate BETWEEN :today AND :endDate
        ORDER BY b.expiryDate ASC
        """)
    List<InventoryBatch> findExpiringSoon(
            @Param("storeId") Long storeId,
            @Param("today") LocalDate today,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT b FROM InventoryBatch b, StoreInventory i
        WHERE b.inventoryId = i.id
          AND i.storeId = :storeId
          AND b.quantityRemaining > 0
          AND b.expiryDate < :today
        ORDER BY b.expiryDate ASC
        """)
    List<InventoryBatch> findExpired(
            @Param("storeId") Long storeId,
            @Param("today") LocalDate today
    );

    @Query("""
        SELECT COALESCE(SUM(b.quantityRemaining), 0)
        FROM InventoryBatch b
        WHERE b.inventoryId = :inventoryId
        """)
    BigDecimal findTotalQuantity(
            @Param("inventoryId") Long inventoryId
    );
}

