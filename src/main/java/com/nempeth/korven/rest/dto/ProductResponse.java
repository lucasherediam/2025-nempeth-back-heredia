package com.nempeth.korven.rest.dto;

import com.nempeth.korven.constants.StockUnit;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal cost,
        CategoryResponse category,
        BigDecimal stockQuantity,
        StockUnit stockUnit,
        BigDecimal reorderPoint) {
}
