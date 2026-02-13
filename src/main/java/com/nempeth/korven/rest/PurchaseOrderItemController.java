package com.nempeth.korven.rest;

import com.nempeth.korven.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/businesses/{businessId}/purchase-orders/{purchaseOrderId}/items")
@RequiredArgsConstructor
public class PurchaseOrderItemController {

    private final PurchaseOrderService purchaseOrderService;

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable UUID businessId,
            @PathVariable UUID purchaseOrderId,
            @PathVariable UUID itemId,
            Authentication auth) {
        String userEmail = auth.getName();
        purchaseOrderService.deleteItem(userEmail, businessId, purchaseOrderId, itemId);
        return ResponseEntity.ok(Map.of("message", "Item eliminado"));
    }
}
