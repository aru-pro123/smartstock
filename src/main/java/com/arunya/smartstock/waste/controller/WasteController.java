package com.arunya.smartstock.waste.controller;


import com.arunya.smartstock.waste.dto.*;
import com.arunya.smartstock.waste.service.WasteService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/waste")
public class WasteController {

    private final WasteService wasteService;

    public WasteController(WasteService wasteService) {
        this.wasteService = wasteService;
    }

    @PostMapping
    public ResponseEntity<WasteResponse> create(
            @Valid @RequestBody WasteRequest request
    ) {

        WasteResponse response = wasteService.create(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/waste/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public WasteResponse getById(
            @PathVariable Long id
    ) {

        return wasteService.findById(id);
    }

    @GetMapping("/inventory/{inventoryId}")
    public List<WasteResponse> getByInventory(
            @PathVariable Long inventoryId
    ) {

        return wasteService.findByInventory(inventoryId);
    }

    @GetMapping("/batch/{batchId}")
    public List<WasteResponse> getByBatch(
            @PathVariable Long batchId
    ) {

        return wasteService.findByBatch(batchId);
    }

    @GetMapping("/store/{storeId}")
    public List<WasteResponse> getByStore(
            @PathVariable Long storeId
    ) {

        return wasteService.findByStore(storeId);
    }

    @GetMapping("/store/{storeId}/summary")
    public WasteSummaryResponse getSummary(
            @PathVariable Long storeId
    ) {

        return wasteService.getSummary(storeId);
    }
}

