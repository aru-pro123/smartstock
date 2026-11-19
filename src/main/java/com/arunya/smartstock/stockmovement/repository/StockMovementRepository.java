package com.arunya.smartstock.stockmovement.repository;


import com.arunya.smartstock.stockmovement.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByInventoryIdOrderByCreatedAtDescIdDesc(
            Long inventoryId);
}
