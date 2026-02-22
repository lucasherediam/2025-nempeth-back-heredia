package com.nempeth.korven.persistence.repository;

import com.nempeth.korven.persistence.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByBusinessId(UUID businessId);

    void deleteByBusinessId(UUID businessId);

    List<Product> findByBusinessIdAndCategoryId(UUID businessId, UUID categoryId);

    Optional<Product> findByIdAndBusinessId(UUID id, UUID businessId);

    boolean existsByBusinessIdAndNameIgnoreCase(UUID businessId, String name);

    boolean existsByCategoryId(UUID categoryId);
}
