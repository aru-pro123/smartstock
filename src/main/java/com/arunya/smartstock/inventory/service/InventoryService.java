package com.arunya.smartstock.inventory.service;

import com.arunya.smartstock.inventory.dto.*;
import com.arunya.smartstock.inventory.entity.StoreInventory;
import com.arunya.smartstock.inventory.repository.InventoryRepository;
import com.arunya.smartstock.product.entity.Product;
import com.arunya.smartstock.product.repository.ProductRepository;
import com.arunya.smartstock.store.entity.Store;
import com.arunya.smartstock.store.repository.StoreRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            StoreRepository storeRepository,
            ProductRepository productRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public InventoryResponse create(InventoryRequest request) {

        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND, "Store not found"));

        if (!store.isActive()) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Store is inactive");
        }

        Product product = productRepository
                .findById(request.productId())
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND, "Product not found"));

        if (!product.isActive()) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Product is inactive");
        }

        if (inventoryRepository.existsByStoreIdAndProductId(
                request.storeId(), request.productId())) {
            throw error(HttpStatus.CONFLICT,
                    "Inventory already exists for this store and product");
        }

        StoreInventory inventory = new StoreInventory();

        inventory.setStoreId(request.storeId());
        inventory.setProductId(request.productId());
        inventory.setQuantityOnHand(BigDecimal.ZERO);
        inventory.setQuantityReserved(BigDecimal.ZERO);
        inventory.setReorderLevel(request.reorderLevel());
        inventory.setReorderQuantity(request.reorderQuantity());
        inventory.setActive(true);

        return InventoryResponse.from(
                inventoryRepository.saveAndFlush(inventory));
    }

    @Transactional(readOnly = true)
    public InventoryResponse findById(Long id) {
        return InventoryResponse.from(
                inventoryRepository.findById(id)
                        .orElseThrow(() -> error(
                                HttpStatus.NOT_FOUND,
                                "Inventory not found")));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findByStore(Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw error(HttpStatus.NOT_FOUND, "Store not found");
        }

        return inventoryRepository.findByStoreId(storeId)
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findLowStock(Long storeId) {

        if (!storeRepository.existsById(storeId)) {
            throw error(HttpStatus.NOT_FOUND, "Store not found");
        }

        return inventoryRepository.findLowStock(storeId)
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }

    @Transactional
    public InventoryResponse updateSettings(
            Long id, InventorySettingsRequest request) {

        StoreInventory inventory = getLocked(id);

        inventory.setReorderLevel(request.reorderLevel());
        inventory.setReorderQuantity(request.reorderQuantity());

        return InventoryResponse.from(
                inventoryRepository.saveAndFlush(inventory));
    }

    @Transactional
    public InventoryResponse updateStatus(
            Long id, InventoryStatusRequest request) {

        StoreInventory inventory = getLocked(id);

        if (request.active()) {
            validateStoreAndProduct(inventory);
        }

        inventory.setActive(request.active());

        return InventoryResponse.from(
                inventoryRepository.saveAndFlush(inventory));
    }

    @Transactional
    public InventoryResponse adjustQuantity(
            Long id, InventoryQuantityRequest request) {

        StoreInventory inventory = getLocked(id);
        validateActive(inventory);

        BigDecimal current = inventory.getQuantityOnHand();

        BigDecimal updated = switch (request.type()) {
            case INCREASE -> current.add(request.quantity());
            case DECREASE -> current.subtract(request.quantity());
        };

        if (updated.signum() < 0) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Insufficient physical stock");
        }

        if (updated.compareTo(inventory.getQuantityReserved()) < 0) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Cannot reduce stock below reserved quantity");
        }

        validatePrecision(updated);

        inventory.setQuantityOnHand(updated);

        return InventoryResponse.from(
                inventoryRepository.saveAndFlush(inventory));
    }

    @Transactional
    public InventoryResponse adjustReservation(
            Long id, InventoryReservationRequest request) {

        StoreInventory inventory = getLocked(id);
        validateActive(inventory);

        BigDecimal current = inventory.getQuantityReserved();

        BigDecimal updated = switch (request.type()) {
            case RESERVE -> current.add(request.quantity());
            case RELEASE -> current.subtract(request.quantity());
        };

        if (updated.signum() < 0) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Cannot release more than reserved quantity");
        }

        if (updated.compareTo(inventory.getQuantityOnHand()) > 0) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Insufficient available stock");
        }

        validatePrecision(updated);

        inventory.setQuantityReserved(updated);

        return InventoryResponse.from(
                inventoryRepository.saveAndFlush(inventory));
    }

    private StoreInventory getLocked(Long id) {
        return inventoryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND, "Inventory not found"));
    }

    private void validateActive(StoreInventory inventory) {

        if (!inventory.getActive()) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Inventory record is inactive");
        }

        validateStoreAndProduct(inventory);
    }

    private void validateStoreAndProduct(StoreInventory inventory) {

        Store store = storeRepository.findById(inventory.getStoreId())
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND, "Store not found"));

        Product product = productRepository
                .findById(inventory.getProductId())
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND, "Product not found"));

        if (!store.isActive() || !product.isActive()) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Store or product is inactive");
        }
    }

    private void validatePrecision(BigDecimal value) {
        if (value.precision() - value.scale() > 9
                || value.scale() > 3) {
            throw error(HttpStatus.BAD_REQUEST,
                    "Quantity exceeds database precision");
        }
    }

    private ResponseStatusException error(
            HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
