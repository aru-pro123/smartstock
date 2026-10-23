package com.arunya.smartstock.store.dto;

import com.arunya.smartstock.store.entity.Store;
import java.time.OffsetDateTime;

public record StoreResponse(
        Long id,
        String storeCode,
        String name,
        String city,
        String province,
        String address,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static StoreResponse from(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getStoreCode(),
                store.getName(),
                store.getCity(),
                store.getProvince(),
                store.getAddress(),
                store.isActive(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }
}