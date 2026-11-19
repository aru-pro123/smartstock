package com.arunya.smartstock.batch.controller;


import com.arunya.smartstock.batch.dto.*;
import com.arunya.smartstock.batch.service.BatchService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping("/receive")
    public ResponseEntity<BatchResponse> receive(
            @Valid @RequestBody BatchReceiveRequest request
    ) {

        BatchResponse response = batchService.receive(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/batches/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public BatchResponse getById(@PathVariable Long id) {
        return batchService.findById(id);
    }

    @GetMapping("/inventory/{inventoryId}")
    public List<BatchResponse> getByInventory(
            @PathVariable Long inventoryId
    ) {
        return batchService.findByInventory(inventoryId);
    }

    @GetMapping("/store/{storeId}/expiring-soon")
    public List<BatchResponse> getExpiringSoon(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "7") int days
    ) {
        return batchService.findExpiringSoon(storeId, days);
    }

    @GetMapping("/store/{storeId}/expired")
    public List<BatchResponse> getExpired(
            @PathVariable Long storeId
    ) {
        return batchService.findExpired(storeId);
    }

    @GetMapping("/{id}/movements")
    public List<BatchMovementResponse> getMovements(
            @PathVariable Long id
    ) {
        return batchService.findMovements(id);
    }
}

