package com.nempeth.korven.service;

import com.nempeth.korven.constants.MembershipStatus;
import com.nempeth.korven.persistence.entity.Business;
import com.nempeth.korven.persistence.entity.BusinessMembership;
import com.nempeth.korven.persistence.entity.Category;
import com.nempeth.korven.persistence.entity.Product;
import com.nempeth.korven.persistence.entity.User;
import com.nempeth.korven.persistence.repository.BusinessMembershipRepository;
import com.nempeth.korven.persistence.repository.BusinessRepository;
import com.nempeth.korven.persistence.repository.CategoryRepository;
import com.nempeth.korven.persistence.repository.ProductRepository;
import com.nempeth.korven.persistence.repository.UserRepository;
import com.nempeth.korven.rest.dto.CategoryResponse;
import com.nempeth.korven.rest.dto.ProductResponse;
import com.nempeth.korven.rest.dto.ProductUpsertRequest;
import com.nempeth.korven.rest.dto.UpdateStockRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @Transactional
    public UUID create(String userEmail, UUID businessId, ProductUpsertRequest req) {
        validateUserBusinessAccess(userEmail, businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        if (!category.getBusiness().getId().equals(businessId)) {
            throw new IllegalArgumentException("La categoría no pertenece a este negocio");
        }

        if (productRepository.existsByBusinessIdAndNameIgnoreCase(businessId, req.name())) {
            throw new IllegalArgumentException("Ya existe un producto con ese nombre en este negocio");
        }

        Product product = Product.builder()
                .business(business)
                .category(category)
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .cost(req.cost())
                .stockQuantity(req.stockQuantity())
                .stockUnit(req.stockUnit())
                .reorderPoint(req.reorderPoint())
                .build();

        product = productRepository.save(product);
        return product.getId();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listByBusiness(String userEmail, UUID businessId) {
        validateUserBusinessAccess(userEmail, businessId);

        return productRepository.findByBusinessId(businessId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listByBusinessAndCategory(String userEmail, UUID businessId, UUID categoryId) {
        validateUserBusinessAccess(userEmail, businessId);

        return productRepository.findByBusinessIdAndCategoryId(businessId, categoryId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void update(String userEmail, UUID businessId, UUID productId, ProductUpsertRequest req) {
        validateUserBusinessAccess(userEmail, businessId);

        Product product = productRepository.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en este negocio"));

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        if (!category.getBusiness().getId().equals(businessId)) {
            throw new IllegalArgumentException("La categoría no pertenece a este negocio");
        }

        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setCost(req.cost());
        product.setCategory(category);
        product.setStockQuantity(req.stockQuantity());
        product.setStockUnit(req.stockUnit());
        product.setReorderPoint(req.reorderPoint());

        productRepository.save(product);
    }

    @Transactional
    public void updateStock(String userEmail, UUID businessId, UUID productId, UpdateStockRequest req) {
        validateUserBusinessAccess(userEmail, businessId);

        Product product = productRepository.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en este negocio"));

        product.setStockQuantity(req.stockQuantity());
        product.setStockUnit(req.unit());
        product.setReorderPoint(req.reorderPoint());

        productRepository.save(product);
    }

    @Transactional
    public void delete(String userEmail, UUID businessId, UUID productId) {
        validateUserBusinessAccess(userEmail, businessId);

        Product product = productRepository.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en este negocio"));

        productRepository.delete(product);
    }

    private void validateUserBusinessAccess(String userEmail, UUID businessId) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        BusinessMembership membership = membershipRepository.findByBusinessIdAndUserId(businessId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No tienes acceso a este negocio"));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new IllegalArgumentException("Tu membresía en este negocio no está activa");
        }
    }

    private ProductResponse mapToResponse(Product product) {
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .type(product.getCategory().getType())
                .displayName(product.getCategory().getDisplayName())
                .icon(product.getCategory().getIcon())
                .build();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .cost(product.getCost())
                .category(categoryResponse)
                .stockQuantity(product.getStockQuantity())
                .stockUnit(product.getStockUnit())
                .reorderPoint(product.getReorderPoint())
                .build();
    }
}
