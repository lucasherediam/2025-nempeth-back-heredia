package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.StockItemResponse;
import com.nempeth.korven.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/businesses/{businessId}/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockItemResponse>> getBusinessStock(
            @PathVariable UUID businessId,
            Authentication auth) {
        String userEmail = auth.getName();
        List<StockItemResponse> stock = stockService.getBusinessStock(userEmail, businessId);
        return ResponseEntity.ok(stock);
    }
}
