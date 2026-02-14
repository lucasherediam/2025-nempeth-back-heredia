package com.nempeth.korven.rest.dto;

import com.nempeth.korven.constants.StockStatus;
import com.nempeth.korven.constants.StockUnit;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record StockItemResponse(
        UUID productId,
        String productName,
        UUID categoryId,
        String categoryName,
        BigDecimal stockQuantity,
        StockUnit stockUnit,
        BigDecimal reorderPoint,
        StockStatus status) {
}
