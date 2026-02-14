package com.nempeth.korven.rest.dto;

import com.nempeth.korven.constants.StockUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpsertRequest(
        @NotBlank(message = "El nombre del producto es obligatorio") @Size(max = 255, message = "El nombre no puede tener más de 255 caracteres") String name,

        @Size(max = 1000, message = "La descripción no puede tener más de 1000 caracteres") String description,

        @NotNull(message = "El precio es obligatorio") @Positive(message = "El precio debe ser mayor a 0") BigDecimal price,

        @NotNull(message = "El costo es obligatorio") @Positive(message = "El costo debe ser mayor a 0") BigDecimal cost,

        @NotNull(message = "La categoría es obligatoria") UUID categoryId,

        @PositiveOrZero(message = "La cantidad de stock debe ser mayor o igual a 0") BigDecimal stockQuantity,

        StockUnit stockUnit,

        @PositiveOrZero(message = "El punto de reorden debe ser mayor o igual a 0") BigDecimal reorderPoint) {
}
