package com.nempeth.korven.persistence.repository;

import com.nempeth.korven.persistence.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {
    List<PurchaseOrderItem> findByPurchaseOrderId(UUID purchaseOrderId);

    List<PurchaseOrderItem> findByProductId(UUID productId);

    @Modifying
    @Query("DELETE FROM PurchaseOrderItem poi WHERE poi.purchaseOrder.id IN (SELECT po.id FROM PurchaseOrder po WHERE po.business.id = :businessId)")
    void deleteByBusinessId(@Param("businessId") UUID businessId);
}
