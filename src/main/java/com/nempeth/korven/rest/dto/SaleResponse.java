package com.nempeth.korven.rest.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record SaleResponse(
        UUID id,
        String note,
        OffsetDateTime occurredAt,
        BigDecimal totalAmount,
        String createdByUserName,
        List<SaleItemResponse> items
) {
}