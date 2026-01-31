package com.nempeth.korven.service;

import com.nempeth.korven.constants.MembershipStatus;
import com.nempeth.korven.constants.StockStatus;
import com.nempeth.korven.persistence.entity.BusinessMembership;
import com.nempeth.korven.persistence.entity.Product;
import com.nempeth.korven.persistence.entity.User;
import com.nempeth.korven.persistence.repository.BusinessMembershipRepository;
import com.nempeth.korven.persistence.repository.ProductRepository;
import com.nempeth.korven.persistence.repository.UserRepository;
import com.nempeth.korven.rest.dto.StockItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BusinessMembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public List<StockItemResponse> getBusinessStock(String userEmail, UUID businessId) {
        validateUserBusinessAccess(userEmail, businessId);

        // Get all products with stock information
        List<Product> products = productRepository.findByBusinessId(businessId);

        return products.stream()
                .map(this::mapToStockItemResponse)
                .toList();
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

    private StockItemResponse mapToStockItemResponse(Product product) {
        return StockItemResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .stockQuantity(product.getStockQuantity())
                .stockUnit(product.getStockUnit())
                .reorderPoint(product.getReorderPoint())
                .status(calculateStockStatus(product.getStockQuantity(), product.getReorderPoint()))
                .build();
    }

    private StockStatus calculateStockStatus(BigDecimal stockQuantity, BigDecimal reorderPoint) {
        if (stockQuantity == null || reorderPoint == null) {
            return StockStatus.OK;
        }
        if (stockQuantity.compareTo(reorderPoint) < 0) {
            return StockStatus.BELOW_MIN;
        }
        // Stock is at or above reorder point but below warning limit (reorder point + 25%)
        BigDecimal warningLimit = reorderPoint.multiply(BigDecimal.valueOf(1.25));
        if (stockQuantity.compareTo(warningLimit) < 0) {
            return StockStatus.LOW;
        }
        return StockStatus.OK;
    }
}
