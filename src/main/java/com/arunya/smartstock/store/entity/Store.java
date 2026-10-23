package com.arunya.smartstock.store.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_code", nullable = false, unique = true, length = 20)
    private String storeCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 50)
    private String province;

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;

    protected Store() {}

    public Store(String storeCode, String name, String city,
                 String province, String address) {
        this.storeCode = storeCode;
        this.name = name;
        this.city = city;
        this.province = province;
        this.address = address;
    }

    public void update(String name, String city,
                       String province, String address) {
        this.name = name;
        this.city = city;
        this.province = province;
        this.address = address;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getStoreCode() { return storeCode; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getAddress() { return address; }
    public boolean isActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}