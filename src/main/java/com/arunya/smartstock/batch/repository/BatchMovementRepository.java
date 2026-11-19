package com.arunya.smartstock.batch.repository;


import com.arunya.smartstock.batch.entity.BatchMovement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchMovementRepository
        extends JpaRepository<BatchMovement, Long> {

    List<BatchMovement> findByBatchIdOrderByCreatedAtDescIdDesc(
            Long batchId
    );

    List<BatchMovement> findByStockMovementId(
            Long stockMovementId
    );
}
