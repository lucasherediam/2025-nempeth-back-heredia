package com.nempeth.korven.persistence.repository;

import com.nempeth.korven.persistence.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {
    List<PurchaseOrderItem> findByPurchaseOrderId(UUID purchaseOrderId);

    List<PurchaseOrderItem> findByProductId(UUID productId);
}
