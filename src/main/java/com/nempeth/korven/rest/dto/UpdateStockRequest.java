package com.nempeth.korven.rest.dto;

import com.nempeth.korven.constants.StockUnit;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateStockRequest(
        @PositiveOrZero(message = "La cantidad de stock debe ser mayor o igual a 0") BigDecimal stockQuantity,

        StockUnit unit,

        @PositiveOrZero(message = "El punto de reorden debe ser mayor o igual a 0") BigDecimal reorderPoint) {
}
