package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.CreatePurchaseOrderRequest;
import com.nempeth.korven.rest.dto.PurchaseOrderListItem;
import com.nempeth.korven.rest.dto.PurchaseOrderResponse;
import com.nempeth.korven.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/businesses/{businessId}/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<?> create(@PathVariable UUID businessId,
            @Valid @RequestBody CreatePurchaseOrderRequest req,
            Authentication auth) {
        String userEmail = auth.getName();
        UUID orderId = purchaseOrderService.create(userEmail, businessId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("purchaseOrderId", orderId.toString()));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderListItem>> list(@PathVariable UUID businessId,
            Authentication auth) {
        String userEmail = auth.getName();
        List<PurchaseOrderListItem> orders = purchaseOrderService.list(userEmail, businessId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{purchaseOrderId}")
    public ResponseEntity<PurchaseOrderResponse> getById(@PathVariable UUID businessId,
            @PathVariable UUID purchaseOrderId,
            Authentication auth) {
        String userEmail = auth.getName();
        PurchaseOrderResponse order = purchaseOrderService.getById(userEmail, businessId, purchaseOrderId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{purchaseOrderId}/receive")
    public ResponseEntity<?> markAsReceived(@PathVariable UUID businessId,
            @PathVariable UUID purchaseOrderId,
            Authentication auth) {
        String userEmail = auth.getName();
        purchaseOrderService.markAsReceived(userEmail, businessId, purchaseOrderId);
        return ResponseEntity.ok(Map.of("message", "Orden marcada como recibida y stock actualizado"));
    }

    @PatchMapping("/{purchaseOrderId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable UUID businessId,
            @PathVariable UUID purchaseOrderId,
            Authentication auth) {
        String userEmail = auth.getName();
        purchaseOrderService.cancel(userEmail, businessId, purchaseOrderId);
        return ResponseEntity.ok(Map.of("message", "Orden cancelada"));
    }
}
