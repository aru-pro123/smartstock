package com.arunya.smartstock.department.controller;

import com.arunya.smartstock.department.dto.*;
import com.arunya.smartstock.department.service.DepartmentService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping("/api/v1/stores/{storeId}/departments")
    public ResponseEntity<DepartmentResponse> create(
            @PathVariable Long storeId,
            @Valid @RequestBody DepartmentRequest request
    ) {

        DepartmentResponse response =
                service.create(storeId, request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/departments/" + response.id()
                ))
                .body(response);
    }

    @GetMapping("/api/v1/stores/{storeId}/departments")
    public List<DepartmentResponse> getByStore(
            @PathVariable Long storeId
    ) {
        return service.findByStore(storeId);
    }

    @GetMapping("/api/v1/departments/{id}")
    public DepartmentResponse getById(
            @PathVariable Long id
    ) {
        return service.findById(id);
    }

    @PutMapping("/api/v1/departments/{id}")
    public DepartmentResponse update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        return service.update(id, request);
    }

    @PatchMapping("/api/v1/departments/{id}/status")
    public DepartmentResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentStatusRequest request
    ) {
        return service.updateStatus(id, request);
    }
}