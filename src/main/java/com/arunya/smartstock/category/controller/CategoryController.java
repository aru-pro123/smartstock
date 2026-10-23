package com.arunya.smartstock.category.controller;

import com.arunya.smartstock.category.dto.*;
import com.arunya.smartstock.category.service.CategoryService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request
    ) {

        CategoryResponse response = service.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/categories/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public CategoryResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CategoryStatusRequest request
    ) {
        return service.updateStatus(id, request);
    }
}