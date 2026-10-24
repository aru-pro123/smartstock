package com.arunya.smartstock.product.entity;

import com.arunya.smartstock.category.entity.Category;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(unique = true, length = 50)
    private String barcode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(nullable = false)
    private boolean taxable;

    @Column(nullable = false)
    private boolean perishable;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;

    protected Product() {}

    public Product(
            String sku,
            String barcode,
            String name,
            String brand,
            Category category,
            String unitOfMeasure,
            BigDecimal sellingPrice,
            boolean taxable,
            boolean perishable,
            Integer shelfLifeDays
    ) {
        this.sku = sku;
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
        this.sellingPrice = sellingPrice;
        this.taxable = taxable;
        this.perishable = perishable;
        this.shelfLifeDays = shelfLifeDays;
    }

    public void update(
            String barcode,
            String name,
            String brand,
            Category category,
            String unitOfMeasure,
            BigDecimal sellingPrice,
            boolean taxable,
            boolean perishable,
            Integer shelfLifeDays
    ) {
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.unitOfMeasure = unitOfMeasure;
        this.sellingPrice = sellingPrice;
        this.taxable = taxable;
        this.perishable = perishable;
        this.shelfLifeDays = shelfLifeDays;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getBarcode() { return barcode; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public Category getCategory() { return category; }
    public String getUnitOfMeasure() { return unitOfMeasure; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public boolean isTaxable() { return taxable; }
    public boolean isPerishable() { return perishable; }
    public Integer getShelfLifeDays() { return shelfLifeDays; }
    public boolean isActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}