package com.nempeth.korven.service;

import com.nempeth.korven.constants.MembershipStatus;
import com.nempeth.korven.constants.PurchaseOrderStatus;
import com.nempeth.korven.persistence.entity.*;
import com.nempeth.korven.persistence.repository.*;
import com.nempeth.korven.rest.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

        private final PurchaseOrderRepository purchaseOrderRepository;
        private final BusinessRepository businessRepository;
        private final ProductRepository productRepository;
        private final UserRepository userRepository;
        private final BusinessMembershipRepository membershipRepository;

        @Transactional
        public UUID create(String userEmail, UUID businessId, CreatePurchaseOrderRequest req) {
                validateUserBusinessAccess(userEmail, businessId);

                Business business = businessRepository.findById(businessId)
                                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

                PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                                .business(business)
                                .supplierName(req.supplierName())
                                .status(PurchaseOrderStatus.PENDING)
                                .build();

                // Add items to purchase order
                for (PurchaseOrderItemRequest itemReq : req.items()) {
                        Product product = productRepository.findById(itemReq.productId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Producto no encontrado: " + itemReq.productId()));

                        if (!product.getBusiness().getId().equals(businessId)) {
                                throw new IllegalArgumentException("El producto no pertenece a este negocio");
                        }

                        PurchaseOrderItem item = PurchaseOrderItem.builder()
                                        .product(product)
                                        .productName(product.getName())
                                        .quantity(itemReq.quantity())
                                        .unitCost(itemReq.unitCost())
                                        .build();

                        purchaseOrder.addItem(item);
                }

                PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
                return saved.getId();
        }

        @Transactional(readOnly = true)
        public List<PurchaseOrderListItem> list(String userEmail, UUID businessId) {
                validateUserBusinessAccess(userEmail, businessId);

                List<PurchaseOrder> orders = purchaseOrderRepository.findByBusinessId(businessId);

                return orders.stream()
                                .map(this::mapToListItem)
                                .toList();
        }

        @Transactional(readOnly = true)
        public PurchaseOrderResponse getById(String userEmail, UUID businessId, UUID purchaseOrderId) {
                validateUserBusinessAccess(userEmail, businessId);

                PurchaseOrder order = purchaseOrderRepository.findByIdAndBusinessId(purchaseOrderId, businessId)
                                .orElseThrow(() -> new IllegalArgumentException("Orden de compra no encontrada"));

                return mapToResponse(order);
        }

        @Transactional
        public void deleteItem(String userEmail, UUID businessId, UUID purchaseOrderId, UUID itemId) {
                validateUserBusinessAccess(userEmail, businessId);

                PurchaseOrder order = purchaseOrderRepository.findByIdAndBusinessId(purchaseOrderId, businessId)
                                .orElseThrow(() -> new IllegalArgumentException("Orden de compra no encontrada"));

                if (order.getStatus() != PurchaseOrderStatus.PENDING) {
                        throw new IllegalArgumentException("Solo se pueden eliminar items de órdenes pendientes");
                }

                PurchaseOrderItem item = order.getItems().stream()
                                .filter(i -> i.getId().equals(itemId))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado en esta orden"));

                order.removeItem(item);
                purchaseOrderRepository.save(order);
        }

        @Transactional
        public void markAsReceived(String userEmail, UUID businessId, UUID purchaseOrderId) {
                validateUserBusinessAccess(userEmail, businessId);

                PurchaseOrder order = purchaseOrderRepository.findByIdAndBusinessId(purchaseOrderId, businessId)
                                .orElseThrow(() -> new IllegalArgumentException("Orden de compra no encontrada"));

                if (order.getStatus() != PurchaseOrderStatus.PENDING) {
                        throw new IllegalArgumentException("La orden ya fue procesada");
                }

                order.setStatus(PurchaseOrderStatus.RECEIVED);
                order.setReceivedAt(OffsetDateTime.now());

                // Update product stock
                int skippedItems = 0;
                for (PurchaseOrderItem item : order.getItems()) {
                        Product product = item.getProduct();

                        // Defensive check: product might have been deleted
                        if (product == null) {
                                skippedItems++;
                                continue;
                        }

                        BigDecimal currentStock = product.getStockQuantity() != null
                                        ? product.getStockQuantity()
                                        : BigDecimal.ZERO;
                        product.setStockQuantity(currentStock.add(item.getQuantity()));
                }

                purchaseOrderRepository.save(order);

                // Log warning if any products were not found
                if (skippedItems > 0) {
                        System.err.println("WARNING: " + skippedItems + " producto(s) no pudieron ser actualizados " +
                                        "en la orden " + purchaseOrderId + " porque fueron eliminados");
                }
        }

        @Transactional
        public void cancel(String userEmail, UUID businessId, UUID purchaseOrderId) {
                validateUserBusinessAccess(userEmail, businessId);

                PurchaseOrder order = purchaseOrderRepository.findByIdAndBusinessId(purchaseOrderId, businessId)
                                .orElseThrow(() -> new IllegalArgumentException("Orden de compra no encontrada"));

                if (order.getStatus() != PurchaseOrderStatus.PENDING) {
                        throw new IllegalArgumentException("Solo se pueden cancelar órdenes pendientes");
                }

                order.setStatus(PurchaseOrderStatus.CANCELLED);
                purchaseOrderRepository.save(order);
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

        private PurchaseOrderListItem mapToListItem(PurchaseOrder order) {
                BigDecimal totalAmount = order.getItems().stream()
                                .map(item -> item.getQuantity().multiply(item.getUnitCost()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return PurchaseOrderListItem.builder()
                                .id(order.getId())
                                .supplierName(order.getSupplierName())
                                .status(order.getStatus())
                                .createdAt(order.getCreatedAt())
                                .receivedAt(order.getReceivedAt())
                                .itemCount(order.getItems().size())
                                .totalAmount(totalAmount)
                                .build();
        }

        private PurchaseOrderResponse mapToResponse(PurchaseOrder order) {
                List<PurchaseOrderItemResponse> items = order.getItems().stream()
                                .map(this::mapItemToResponse)
                                .toList();

                BigDecimal totalAmount = items.stream()
                                .map(PurchaseOrderItemResponse::totalCost)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return PurchaseOrderResponse.builder()
                                .id(order.getId())
                                .supplierName(order.getSupplierName())
                                .status(order.getStatus())
                                .createdAt(order.getCreatedAt())
                                .receivedAt(order.getReceivedAt())
                                .items(items)
                                .totalAmount(totalAmount)
                                .build();
        }

        private PurchaseOrderItemResponse mapItemToResponse(PurchaseOrderItem item) {
                BigDecimal totalCost = item.getQuantity().multiply(item.getUnitCost());

                return PurchaseOrderItemResponse.builder()
                                .id(item.getId())
                                .productId(item.getProduct().getId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitCost(item.getUnitCost())
                                .totalCost(totalCost)
                                .build();
        }
}
