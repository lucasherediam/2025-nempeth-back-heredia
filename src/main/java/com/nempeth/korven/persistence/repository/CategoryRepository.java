package com.nempeth.korven.persistence.repository;

import com.nempeth.korven.constants.CategoryType;
import com.nempeth.korven.persistence.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByBusinessId(UUID businessId);

    void deleteByBusinessId(UUID businessId);
    
    List<Category> findByBusinessIdAndType(UUID businessId, CategoryType type);
    
    boolean existsByBusinessIdAndNameIgnoreCase(UUID businessId, String name);
}