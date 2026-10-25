package com.arunya.smartstock.category.dto;

import com.arunya.smartstock.category.entity.Category;
import java.time.OffsetDateTime;

public record CategoryResponse(
        Long id,
        String categoryCode,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getCategoryCode(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}