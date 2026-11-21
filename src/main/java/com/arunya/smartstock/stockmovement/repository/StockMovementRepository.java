package com.arunya.smartstock.stockmovement.repository;


import com.arunya.smartstock.stockmovement.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import com.arunya.smartstock.stockmovement.entity.StockMovementType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

import java.util.List;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByInventoryIdOrderByCreatedAtDescIdDesc(
            Long inventoryId);


    @Query("""
    SELECT m.movementType,
           COUNT(m),
           SUM(m.quantityChange)
    FROM StockMovement m, StoreInventory i
    WHERE m.inventoryId = i.id
      AND i.storeId = :storeId
      AND m.createdAt >= :from
      AND m.createdAt < :toExclusive
    GROUP BY m.movementType
    """)
    List<Object[]> summarizeMovements(
            @Param("storeId") Long storeId,
            @Param("from") OffsetDateTime from,
            @Param("toExclusive") OffsetDateTime toExclusive
    );

}
