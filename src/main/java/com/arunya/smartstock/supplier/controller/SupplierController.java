package com.arunya.smartstock.supplier.controller;


import com.arunya.smartstock.supplier.dto.*;
import com.arunya.smartstock.supplier.service.SupplierService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(
            SupplierService supplierService
    ) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse createSupplier(
            @Valid @RequestBody SupplierCreateRequest request
    ) {
        return supplierService.createSupplier(request);
    }

    @GetMapping
    public List<SupplierResponse> getAllSuppliers(
            @RequestParam(defaultValue = "false")
            boolean activeOnly
    ) {
        return supplierService.getAllSuppliers(activeOnly);
    }

    @GetMapping("/{id}")
    public SupplierResponse getSupplierById(
            @PathVariable Long id
    ) {
        return supplierService.getSupplierById(id);
    }

    @PutMapping("/{id}")
    public SupplierResponse updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierUpdateRequest request
    ) {
        return supplierService.updateSupplier(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public SupplierResponse deactivateSupplier(
            @PathVariable Long id
    ) {
        return supplierService.deactivateSupplier(id);
    }

    @PatchMapping("/{id}/activate")
    public SupplierResponse activateSupplier(
            @PathVariable Long id
    ) {
        return supplierService.activateSupplier(id);
    }
}