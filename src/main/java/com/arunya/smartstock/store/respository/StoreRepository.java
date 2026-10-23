package com.arunya.smartstock.store.respository;

import com.arunya.smartstock.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

    boolean existsByStoreCode(String storeCode);
}