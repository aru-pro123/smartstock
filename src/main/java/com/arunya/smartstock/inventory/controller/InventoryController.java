package com.arunya.smartstock.inventory.controller;

import com.arunya.smartstock.inventory.dto.*;
import com.arunya.smartstock.inventory.service.InventoryService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> create(
            @Valid @RequestBody InventoryRequest request) {

        InventoryResponse response = service.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/inventory/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public InventoryResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/store/{storeId}")
    public List<InventoryResponse> getByStore(
            @PathVariable Long storeId) {
        return service.findByStore(storeId);
    }

    @GetMapping("/store/{storeId}/low-stock")
    public List<InventoryResponse> getLowStock(
            @PathVariable Long storeId) {
        return service.findLowStock(storeId);
    }

    @PutMapping("/{id}")
    public InventoryResponse updateSettings(
            @PathVariable Long id,
            @Valid @RequestBody InventorySettingsRequest request) {
        return service.updateSettings(id, request);
    }

    @PatchMapping("/{id}/status")
    public InventoryResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody InventoryStatusRequest request) {
        return service.updateStatus(id, request);
    }

    @PatchMapping("/{id}/quantity")
    public InventoryResponse adjustQuantity(
            @PathVariable Long id,
            @Valid @RequestBody InventoryQuantityRequest request) {
        return service.adjustQuantity(id, request);
    }

    @PatchMapping("/{id}/reservation")
    public InventoryResponse adjustReservation(
            @PathVariable Long id,
            @Valid @RequestBody InventoryReservationRequest request) {
        return service.adjustReservation(id, request);
    }
}
