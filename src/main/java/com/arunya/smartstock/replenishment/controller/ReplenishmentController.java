package com.arunya.smartstock.replenishment.controller;

import com.arunya.smartstock.replenishment.dto.ReplenishmentResponse;
import com.arunya.smartstock.replenishment.dto.ReplenishmentOrderRequest;
import com.arunya.smartstock.replenishment.service.ReplenishmentService;

import com.arunya.smartstock.purchaseorder.dto.PurchaseOrderResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/replenishment")
public class ReplenishmentController {

    private final ReplenishmentService replenishmentService;

    public ReplenishmentController(
            ReplenishmentService replenishmentService
    ) {
        this.replenishmentService = replenishmentService;
    }

    @GetMapping("/store/{storeId}")
    public List<ReplenishmentResponse> findByStore(
            @PathVariable Long storeId
    ) {
        return replenishmentService.findByStore(storeId);
    }

    @GetMapping("/inventory/{inventoryId}")
    public ReplenishmentResponse findByInventory(
            @PathVariable Long inventoryId
    ) {
        return replenishmentService.findByInventory(inventoryId);
    }

    @PostMapping("/purchase-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse generatePurchaseOrder(
            @Valid @RequestBody ReplenishmentOrderRequest request
    ) {
        return replenishmentService.generatePurchaseOrder(request);
    }
}
