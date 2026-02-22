package com.nempeth.korven.service;

import com.nempeth.korven.constants.MembershipRole;
import com.nempeth.korven.constants.MembershipStatus;
import com.nempeth.korven.constants.PurchaseOrderStatus;
import com.nempeth.korven.persistence.entity.*;
import com.nempeth.korven.persistence.repository.*;
import com.nempeth.korven.rest.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderService Tests")
class PurchaseOrderServiceTest {

        @Mock
        private PurchaseOrderRepository purchaseOrderRepository;

        @Mock
        private BusinessRepository businessRepository;

        @Mock
        private ProductRepository productRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private BusinessMembershipRepository membershipRepository;

        @InjectMocks
        private PurchaseOrderService purchaseOrderService;

        private String userEmail;
        private UUID userId;
        private UUID businessId;
        private UUID productId;
        private UUID orderId;
        private User testUser;
        private Business testBusiness;
        private Product testProduct;
        private BusinessMembership testMembership;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                userId = UUID.randomUUID();
                businessId = UUID.randomUUID();
                productId = UUID.randomUUID();
                orderId = UUID.randomUUID();

                testUser = User.builder()
                                .id(userId)
                                .email(userEmail)
                                .name("Test")
                                .lastName("User")
                                .passwordHash("hashedpassword")
                                .build();

                testBusiness = Business.builder()
                                .id(businessId)
                                .name("Test Business")
                                .joinCode("TEST123")
                                .build();

                testProduct = Product.builder()
                                .id(productId)
                                .business(testBusiness)
                                .name("Harina")
                                .price(new BigDecimal("100.00"))
                                .cost(new BigDecimal("50.00"))
                                .stockQuantity(new BigDecimal("10"))
                                .build();

                testMembership = BusinessMembership.builder()
                                .user(testUser)
                                .business(testBusiness)
                                .role(MembershipRole.OWNER)
                                .status(MembershipStatus.ACTIVE)
                                .build();
        }

        private void setupValidAccess() {
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));
        }

        private PurchaseOrder buildTestOrder(PurchaseOrderStatus status) {
                PurchaseOrder order = PurchaseOrder.builder()
                                .id(orderId)
                                .business(testBusiness)
                                .supplierName("Proveedor Test")
                                .status(status)
                                .items(new ArrayList<>())
                                .build();
                return order;
        }

        private PurchaseOrderItem buildTestItem(PurchaseOrder order) {
                PurchaseOrderItem item = PurchaseOrderItem.builder()
                                .id(UUID.randomUUID())
                                .purchaseOrder(order)
                                .product(testProduct)
                                .productId(productId)
                                .productName("Harina")
                                .quantity(new BigDecimal("5"))
                                .unitCost(new BigDecimal("50.00"))
                                .build();
                order.getItems().add(item);
                return item;
        }

        // ==================== CREATE ====================

        @Test
        @DisplayName("Should create purchase order successfully")
        void shouldCreatePurchaseOrderSuccessfully() {
                // Given
                PurchaseOrderItemRequest itemReq = new PurchaseOrderItemRequest(
                                productId, new BigDecimal("10"), new BigDecimal("50.00"));
                CreatePurchaseOrderRequest req = new CreatePurchaseOrderRequest(
                                "Proveedor ABC", List.of(itemReq));

                setupValidAccess();
                when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
                when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

                PurchaseOrder savedOrder = buildTestOrder(PurchaseOrderStatus.PENDING);
                when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(savedOrder);

                // When
                UUID result = purchaseOrderService.create(userEmail, businessId, req);

                // Then
                assertThat(result).isEqualTo(orderId);
                verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
        }

        @Test
        @DisplayName("Should throw exception when business not found on create")
        void shouldThrowExceptionWhenBusinessNotFoundOnCreate() {
                // Given
                PurchaseOrderItemRequest itemReq = new PurchaseOrderItemRequest(
                                productId, new BigDecimal("10"), new BigDecimal("50.00"));
                CreatePurchaseOrderRequest req = new CreatePurchaseOrderRequest(
                                "Proveedor ABC", List.of(itemReq));

                setupValidAccess();
                when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.create(userEmail, businessId, req))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Negocio no encontrado");
        }

        @Test
        @DisplayName("Should throw exception when product not found on create")
        void shouldThrowExceptionWhenProductNotFoundOnCreate() {
                // Given
                PurchaseOrderItemRequest itemReq = new PurchaseOrderItemRequest(
                                productId, new BigDecimal("10"), new BigDecimal("50.00"));
                CreatePurchaseOrderRequest req = new CreatePurchaseOrderRequest(
                                "Proveedor ABC", List.of(itemReq));

                setupValidAccess();
                when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
                when(productRepository.findById(productId)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.create(userEmail, businessId, req))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Producto no encontrado");
        }

        @Test
        @DisplayName("Should throw exception when product belongs to different business on create")
        void shouldThrowExceptionWhenProductFromDifferentBusinessOnCreate() {
                // Given
                Business otherBusiness = Business.builder()
                                .id(UUID.randomUUID())
                                .name("Other Business")
                                .build();
                Product otherProduct = Product.builder()
                                .id(productId)
                                .business(otherBusiness)
                                .name("Otro Producto")
                                .build();

                PurchaseOrderItemRequest itemReq = new PurchaseOrderItemRequest(
                                productId, new BigDecimal("10"), new BigDecimal("50.00"));
                CreatePurchaseOrderRequest req = new CreatePurchaseOrderRequest(
                                "Proveedor ABC", List.of(itemReq));

                setupValidAccess();
                when(businessRepository.findById(businessId)).thenReturn(Optional.of(testBusiness));
                when(productRepository.findById(productId)).thenReturn(Optional.of(otherProduct));

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.create(userEmail, businessId, req))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("El producto no pertenece a este negocio");
        }

        // ==================== LIST ====================

        @Test
        @DisplayName("Should list purchase orders successfully")
        void shouldListPurchaseOrdersSuccessfully() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.PENDING);
                buildTestItem(order);

                setupValidAccess();
                when(purchaseOrderRepository.findByBusinessId(businessId)).thenReturn(List.of(order));

                // When
                List<PurchaseOrderListItem> result = purchaseOrderService.list(userEmail, businessId);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).id()).isEqualTo(orderId);
                assertThat(result.get(0).supplierName()).isEqualTo("Proveedor Test");
                assertThat(result.get(0).status()).isEqualTo(PurchaseOrderStatus.PENDING);
                assertThat(result.get(0).itemCount()).isEqualTo(1);
                assertThat(result.get(0).totalAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        }

        @Test
        @DisplayName("Should return empty list when no purchase orders")
        void shouldReturnEmptyListWhenNoPurchaseOrders() {
                // Given
                setupValidAccess();
                when(purchaseOrderRepository.findByBusinessId(businessId)).thenReturn(List.of());

                // When
                List<PurchaseOrderListItem> result = purchaseOrderService.list(userEmail, businessId);

                // Then
                assertThat(result).isEmpty();
        }

        // ==================== GET BY ID ====================

        @Test
        @DisplayName("Should get purchase order by id successfully")
        void shouldGetPurchaseOrderByIdSuccessfully() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.PENDING);
                buildTestItem(order);

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));

                // When
                PurchaseOrderResponse result = purchaseOrderService.getById(userEmail, businessId, orderId);

                // Then
                assertThat(result.id()).isEqualTo(orderId);
                assertThat(result.supplierName()).isEqualTo("Proveedor Test");
                assertThat(result.items()).hasSize(1);
                assertThat(result.items().get(0).productName()).isEqualTo("Harina");
                assertThat(result.items().get(0).quantity()).isEqualByComparingTo(new BigDecimal("5"));
                assertThat(result.items().get(0).unitCost()).isEqualByComparingTo(new BigDecimal("50.00"));
                assertThat(result.items().get(0).totalCost()).isEqualByComparingTo(new BigDecimal("250.00"));
                assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        }

        @Test
        @DisplayName("Should throw exception when purchase order not found on getById")
        void shouldThrowExceptionWhenOrderNotFoundOnGetById() {
                // Given
                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.getById(userEmail, businessId, orderId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Orden de compra no encontrada");
        }

        // ==================== DELETE ITEM ====================

        @Test
        @DisplayName("Should delete item from pending order successfully")
        void shouldDeleteItemFromPendingOrderSuccessfully() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.PENDING);
                PurchaseOrderItem item = buildTestItem(order);
                UUID itemId = item.getId();

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));

                // When
                purchaseOrderService.deleteItem(userEmail, businessId, orderId, itemId);

                // Then
                assertThat(order.getItems()).isEmpty();
                verify(purchaseOrderRepository).save(order);
        }

        @Test
        @DisplayName("Should throw exception when deleting item from non-pending order")
        void shouldThrowExceptionWhenDeletingItemFromNonPendingOrder() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.RECEIVED);

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.deleteItem(userEmail, businessId, orderId, UUID.randomUUID()))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Solo se pueden eliminar items de órdenes pendientes");
        }

        @Test
        @DisplayName("Should throw exception when item not found in order")
        void shouldThrowExceptionWhenItemNotFoundInOrder() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.PENDING);

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.deleteItem(userEmail, businessId, orderId, UUID.randomUUID()))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Item no encontrado en esta orden");
        }

        @Test
        @DisplayName("Should throw exception when order not found on deleteItem")
        void shouldThrowExceptionWhenOrderNotFoundOnDeleteItem() {
                // Given
                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.deleteItem(userEmail, businessId, orderId, UUID.randomUUID()))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Orden de compra no encontrada");
        }

        // ==================== MARK AS RECEIVED ====================

        @Test
        @DisplayName("Should mark order as received and update stock")
        void shouldMarkAsReceivedAndUpdateStock() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.PENDING);
                buildTestItem(order); // qty=5, product stock=10

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));
                when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

                // When
                purchaseOrderService.markAsReceived(userEmail, businessId, orderId);

                // Then
                assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
                assertThat(order.getReceivedAt()).isNotNull();
                assertThat(testProduct.getStockQuantity()).isEqualByComparingTo(new BigDecimal("15")); // 10 + 5
                verify(purchaseOrderRepository).save(order);
        }

        @Test
        @DisplayName("Should mark as received when product stock is null")
        void shouldMarkAsReceivedWhenProductStockIsNull() {
                // Given
                testProduct.setStockQuantity(null);
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.PENDING);
                buildTestItem(order); // qty=5

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));
                when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

                // When
                purchaseOrderService.markAsReceived(userEmail, businessId, orderId);

                // Then
                assertThat(testProduct.getStockQuantity()).isEqualByComparingTo(new BigDecimal("5")); // 0 + 5
        }

        @Test
        @DisplayName("Should skip item when product is null on markAsReceived")
        void shouldSkipItemWhenProductIsNullOnMarkAsReceived() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.PENDING);
                PurchaseOrderItem item = PurchaseOrderItem.builder()
                                .id(UUID.randomUUID())
                                .purchaseOrder(order)
                                .product(null) // Deleted product
                                .productName("Producto eliminado")
                                .quantity(new BigDecimal("5"))
                                .unitCost(new BigDecimal("50.00"))
                                .build();
                order.getItems().add(item);

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));

                // When
                purchaseOrderService.markAsReceived(userEmail, businessId, orderId);

                // Then
                assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
                verify(purchaseOrderRepository).save(order);
        }

        @Test
        @DisplayName("Should throw exception when marking non-pending order as received")
        void shouldThrowExceptionWhenMarkingNonPendingAsReceived() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.RECEIVED);

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.markAsReceived(userEmail, businessId, orderId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("La orden ya fue procesada");
        }

        @Test
        @DisplayName("Should throw exception when marking cancelled order as received")
        void shouldThrowExceptionWhenMarkingCancelledAsReceived() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.CANCELLED);

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.markAsReceived(userEmail, businessId, orderId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("La orden ya fue procesada");
        }

        @Test
        @DisplayName("Should throw exception when order not found on markAsReceived")
        void shouldThrowExceptionWhenOrderNotFoundOnMarkAsReceived() {
                // Given
                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.markAsReceived(userEmail, businessId, orderId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Orden de compra no encontrada");
        }

        // ==================== CANCEL ====================

        @Test
        @DisplayName("Should cancel pending order successfully")
        void shouldCancelPendingOrderSuccessfully() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.PENDING);

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));

                // When
                purchaseOrderService.cancel(userEmail, businessId, orderId);

                // Then
                assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
                verify(purchaseOrderRepository).save(order);
        }

        @Test
        @DisplayName("Should throw exception when cancelling non-pending order")
        void shouldThrowExceptionWhenCancellingNonPendingOrder() {
                // Given
                PurchaseOrder order = buildTestOrder(PurchaseOrderStatus.RECEIVED);

                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.of(order));

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.cancel(userEmail, businessId, orderId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Solo se pueden cancelar órdenes pendientes");
        }

        @Test
        @DisplayName("Should throw exception when order not found on cancel")
        void shouldThrowExceptionWhenOrderNotFoundOnCancel() {
                // Given
                setupValidAccess();
                when(purchaseOrderRepository.findByIdAndBusinessId(orderId, businessId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.cancel(userEmail, businessId, orderId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Orden de compra no encontrada");
        }

        // ==================== VALIDATE ACCESS ====================

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
                // Given
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.list(userEmail, businessId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Usuario no encontrado");
        }

        @Test
        @DisplayName("Should throw exception when user has no access to business")
        void shouldThrowExceptionWhenUserHasNoAccess() {
                // Given
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.list(userEmail, businessId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("No tienes acceso a este negocio");
        }

        @Test
        @DisplayName("Should throw exception when membership is not active")
        void shouldThrowExceptionWhenMembershipNotActive() {
                // Given
                testMembership.setStatus(MembershipStatus.PENDING);
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.list(userEmail, businessId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Tu membresía en este negocio no está activa");
        }

        @Test
        @DisplayName("Should throw exception when employee tries to create purchase order")
        void shouldThrowExceptionWhenEmployeeTriesToCreatePurchaseOrder() {
                // Given
                BusinessMembership employeeMembership = BusinessMembership.builder()
                                .user(testUser)
                                .business(testBusiness)
                                .role(MembershipRole.EMPLOYEE)
                                .status(MembershipStatus.ACTIVE)
                                .build();

                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(employeeMembership));

                CreatePurchaseOrderRequest req = new CreatePurchaseOrderRequest("Proveedor", List.of());

                // When & Then
                assertThatThrownBy(() -> purchaseOrderService.create(userEmail, businessId, req))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Solo el dueño del negocio puede realizar esta acción");
        }
}
