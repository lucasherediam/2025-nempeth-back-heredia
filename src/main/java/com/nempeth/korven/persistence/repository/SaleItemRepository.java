package com.nempeth.korven.persistence.repository;

import com.nempeth.korven.persistence.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {
    List<SaleItem> findBySaleId(UUID saleId);
    
    List<SaleItem> findByProductId(UUID productId);
    
    Optional<SaleItem> findBySaleIdAndProductId(UUID saleId, UUID productId);

    @Modifying
    @Query("DELETE FROM SaleItem si WHERE si.sale.id IN (SELECT s.id FROM Sale s WHERE s.business.id = :businessId)")
    void deleteByBusinessId(@Param("businessId") UUID businessId);
}