package com.arunya.smartstock.product.controller;

import com.arunya.smartstock.product.dto.*;
import com.arunya.smartstock.product.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = service.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/products/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam String name) {
        return service.search(name);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductResponse> getByCategory(
            @PathVariable Long categoryId
    ) {
        return service.findByCategory(categoryId);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public ProductResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductStatusRequest request
    ) {
        return service.updateStatus(id, request);
    }
}