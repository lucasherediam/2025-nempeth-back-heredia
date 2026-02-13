package com.nempeth.korven.rest.dto;

import com.nempeth.korven.constants.PurchaseOrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record PurchaseOrderListItem(
        UUID id,
        String supplierName,
        PurchaseOrderStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime receivedAt,
        int itemCount,
        BigDecimal totalAmount) {
}
