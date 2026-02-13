package com.nempeth.korven.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemRequest(
        @NotNull(message = "El ID del producto es requerido") UUID productId,

        @NotNull(message = "La cantidad es requerida") @Positive(message = "La cantidad debe ser mayor a 0") BigDecimal quantity,

        @NotNull(message = "El costo unitario es requerido") @Positive(message = "El costo unitario debe ser mayor a 0") BigDecimal unitCost) {
}
