package com.arunya.smartstock.store.controller;


import com.arunya.smartstock.store.dto.*;
import com.arunya.smartstock.store.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService service;

    public StoreController(StoreService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StoreResponse> create(
            @Valid @RequestBody StoreRequest request) {

        StoreResponse response = service.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/stores/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<StoreResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public StoreResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public StoreResponse update(
            @PathVariable Long id,
            @Valid @RequestBody StoreRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public StoreResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StoreStatusRequest request) {
        return service.updateStatus(id, request);
    }
}