package com.nempeth.korven.rest.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PurchaseOrderItemResponse(
        UUID id,
        UUID productId,
        String productName,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalCost) {
}
