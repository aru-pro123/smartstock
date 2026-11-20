package com.arunya.smartstock.inventory.repository;

import com.arunya.smartstock.inventory.entity.StoreInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<StoreInventory, Long> {

    boolean existsByStoreIdAndProductId(
            Long storeId, Long productId);

    List<StoreInventory> findByStoreId(Long storeId);

    @Query("""
        SELECT i FROM StoreInventory i
        WHERE i.storeId = :storeId
          AND i.active = true
          AND (
              COALESCE(
                  (SELECT SUM(b.quantityRemaining)
                   FROM InventoryBatch b
                   WHERE b.inventoryId = i.id
                     AND (
                         b.expiryDate IS NULL
                         OR b.expiryDate >= :today
                     )
                  ), 0
              ) - i.quantityReserved
          ) <= i.reorderLevel
        """)
    List<StoreInventory> findLowStock(
            @Param("storeId") Long storeId,
            @Param("today") LocalDate today);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM StoreInventory i WHERE i.id = :id")
    Optional<StoreInventory> findByIdForUpdate(
            @Param("id") Long id);
}