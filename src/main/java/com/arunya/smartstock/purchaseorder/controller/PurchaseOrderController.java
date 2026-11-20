package com.arunya.smartstock.purchaseorder.controller;


import com.arunya.smartstock.purchaseorder.dto.*;
import com.arunya.smartstock.purchaseorder.service.PurchaseOrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(
            PurchaseOrderService purchaseOrderService
    ) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(
            @Valid @RequestBody PurchaseOrderCreateRequest request
    ) {
        return purchaseOrderService.create(request);
    }

    @GetMapping
    public List<PurchaseOrderResponse> findAll() {
        return purchaseOrderService.findAll();
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse findById(
            @PathVariable Long id
    ) {
        return purchaseOrderService.findById(id);
    }

    @GetMapping("/store/{storeId}")
    public List<PurchaseOrderResponse> findByStore(
            @PathVariable Long storeId
    ) {
        return purchaseOrderService.findByStore(storeId);
    }

    @GetMapping("/supplier/{supplierId}")
    public List<PurchaseOrderResponse> findBySupplier(
            @PathVariable Long supplierId
    ) {
        return purchaseOrderService.findBySupplier(supplierId);
    }

    @PatchMapping("/{id}/order")
    public PurchaseOrderResponse placeOrder(
            @PathVariable Long id
    ) {
        return purchaseOrderService.placeOrder(id);
    }

    @PostMapping("/{id}/receive")
    public PurchaseOrderResponse receive(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderReceiveRequest request
    ) {
        return purchaseOrderService.receive(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public PurchaseOrderResponse cancel(
            @PathVariable Long id
    ) {
        return purchaseOrderService.cancel(id);
    }
}

