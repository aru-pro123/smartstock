package com.arunya.smartstock.waste.repository;


import com.arunya.smartstock.waste.dto.WasteReasonSummary;
import com.arunya.smartstock.waste.entity.WasteRecord;
import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WasteRepository
        extends JpaRepository<WasteRecord, Long> {

    List<WasteRecord> findByInventoryIdOrderByCreatedAtDescIdDesc(
            Long inventoryId
    );

    List<WasteRecord> findByBatchIdOrderByCreatedAtDescIdDesc(
            Long batchId
    );

    @Query("""
        SELECT w
        FROM WasteRecord w, StoreInventory i
        WHERE w.inventoryId = i.id
          AND i.storeId = :storeId
        ORDER BY w.createdAt DESC, w.id DESC
        """)
    List<WasteRecord> findByStoreId(
            @Param("storeId") Long storeId
    );

    @Query("""
        SELECT new com.arunya.smartstock.waste.dto.WasteReasonSummary(
            w.wasteReason,
            COUNT(w),
            SUM(w.quantity),
            SUM(w.totalLoss)
        )
        FROM WasteRecord w, StoreInventory i
        WHERE w.inventoryId = i.id
          AND i.storeId = :storeId
        GROUP BY w.wasteReason
        ORDER BY SUM(w.totalLoss) DESC
        """)
    List<WasteReasonSummary> summarizeByStoreId(
            @Param("storeId") Long storeId
    );


    @Query("""
    SELECT w.wasteReason,
           COUNT(w),
           SUM(w.quantity),
           SUM(w.totalLoss)
    FROM WasteRecord w, StoreInventory i
    WHERE w.inventoryId = i.id
      AND i.storeId = :storeId
      AND w.createdAt >= :from
      AND w.createdAt < :toExclusive
    GROUP BY w.wasteReason
    """)
    List<Object[]> summarizeWasteInPeriod(
            @Param("storeId") Long storeId,
            @Param("from") OffsetDateTime from,
            @Param("toExclusive") OffsetDateTime toExclusive
    );

}

