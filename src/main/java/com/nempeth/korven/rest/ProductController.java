package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.ProductResponse;
import com.nempeth.korven.rest.dto.ProductUpsertRequest;
import com.nempeth.korven.rest.dto.UpdateStockRequest;
import com.nempeth.korven.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/businesses/{businessId}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> create(@PathVariable UUID businessId,
            @Valid @RequestBody ProductUpsertRequest req,
            Authentication auth) {
        String userEmail = auth.getName();
        UUID productId = productService.create(userEmail, businessId, req);
        return ResponseEntity.ok(Map.of("productId", productId.toString()));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(@PathVariable UUID businessId,
            @RequestParam(required = false) UUID categoryId,
            Authentication auth) {
        String userEmail = auth.getName();
        List<ProductResponse> products;

        if (categoryId != null) {
            products = productService.listByBusinessAndCategory(userEmail, businessId, categoryId);
        } else {
            products = productService.listByBusiness(userEmail, businessId);
        }

        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> update(@PathVariable UUID businessId,
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpsertRequest req,
            Authentication auth) {
        String userEmail = auth.getName();
        productService.update(userEmail, businessId, productId, req);
        return ResponseEntity.ok(Map.of("message", "Producto actualizado"));
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<?> updateStock(@PathVariable UUID businessId,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateStockRequest req,
            Authentication auth) {
        String userEmail = auth.getName();
        productService.updateStock(userEmail, businessId, productId, req);
        return ResponseEntity.ok(Map.of("message", "Stock actualizado"));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> delete(@PathVariable UUID businessId,
            @PathVariable UUID productId,
            Authentication auth) {
        String userEmail = auth.getName();
        productService.delete(userEmail, businessId, productId);
        return ResponseEntity.ok(Map.of("message", "Producto eliminado"));
    }
}
