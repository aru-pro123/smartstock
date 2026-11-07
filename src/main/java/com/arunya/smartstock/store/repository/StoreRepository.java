package com.arunya.smartstock.store.repository;

import com.arunya.smartstock.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    boolean existsByStoreCode(String storeCode);
}