package com.nempeth.korven.persistence.repository;

import com.nempeth.korven.constants.PurchaseOrderStatus;
import com.nempeth.korven.persistence.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findByBusinessId(UUID businessId);

    List<PurchaseOrder> findByBusinessIdAndStatus(UUID businessId, PurchaseOrderStatus status);

    Optional<PurchaseOrder> findByIdAndBusinessId(UUID id, UUID businessId);
}
