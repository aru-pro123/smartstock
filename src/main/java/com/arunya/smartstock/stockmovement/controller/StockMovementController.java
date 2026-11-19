package com.arunya.smartstock.stockmovement.controller;

import com.arunya.smartstock.stockmovement.dto.StockMovementRequest;
import com.arunya.smartstock.stockmovement.dto.StockMovementResponse;
import com.arunya.smartstock.stockmovement.service.StockMovementService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-movements")
public class StockMovementController {

    private final StockMovementService service;

    public StockMovementController(StockMovementService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<StockMovementResponse> create(
            @Valid @RequestBody StockMovementRequest request) {

        StockMovementResponse response = service.create(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/stock-movements/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public StockMovementResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/inventory/{inventoryId}")
    public List<StockMovementResponse> getByInventory(
            @PathVariable Long inventoryId) {
        return service.findByInventory(inventoryId);
    }
}
