package com.arunya.smartstock.product.dto;

import com.arunya.smartstock.product.entity.Product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String barcode,
        String name,
        String brand,
        Long categoryId,
        String categoryName,
        String unitOfMeasure,
        BigDecimal sellingPrice,
        boolean taxable,
        boolean perishable,
        Integer shelfLifeDays,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getBarcode(),
                product.getName(),
                product.getBrand(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getUnitOfMeasure(),
                product.getSellingPrice(),
                product.isTaxable(),
                product.isPerishable(),
                product.getShelfLifeDays(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}