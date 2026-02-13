package com.nempeth.korven.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePurchaseOrderRequest(
        @NotBlank(message = "El nombre del proveedor es requerido") @Size(max = 255, message = "El nombre del proveedor no puede tener más de 255 caracteres") String supplierName,

        @NotEmpty(message = "Debe incluir al menos un producto") @Valid List<PurchaseOrderItemRequest> items) {
}
