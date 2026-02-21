package com.nempeth.korven.rest;

import com.nempeth.korven.constants.PurchaseOrderStatus;
import com.nempeth.korven.rest.dto.CreatePurchaseOrderRequest;
import com.nempeth.korven.rest.dto.PurchaseOrderItemRequest;
import com.nempeth.korven.rest.dto.PurchaseOrderListItem;
import com.nempeth.korven.rest.dto.PurchaseOrderResponse;
import com.nempeth.korven.service.PurchaseOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderController Tests")
class PurchaseOrderControllerTest {

        @Mock
        private PurchaseOrderService purchaseOrderService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private PurchaseOrderController purchaseOrderController;

        private String userEmail;
        private UUID businessId;
        private UUID orderId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                orderId = UUID.randomUUID();
                when(authentication.getName()).thenReturn(userEmail);
        }

        // ==================== CREATE ====================

        @Test
        @DisplayName("Should create purchase order and return 201")
        @SuppressWarnings("unchecked")
        void shouldCreatePurchaseOrderAndReturn201() {
                // Given
                UUID productId = UUID.randomUUID();
                CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                                "Proveedor ABC",
                                List.of(new PurchaseOrderItemRequest(productId, new BigDecimal("10"), new BigDecimal("500"))));
                when(purchaseOrderService.create(userEmail, businessId, request)).thenReturn(orderId);

                // When
                ResponseEntity<?> response = purchaseOrderController.create(businessId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("purchaseOrderId")).isEqualTo(orderId.toString());
                verify(purchaseOrderService).create(userEmail, businessId, request);
        }

        // ==================== LIST ====================

        @Test
        @DisplayName("Should return list of purchase orders")
        void shouldReturnListOfPurchaseOrders() {
                // Given
                PurchaseOrderListItem item = PurchaseOrderListItem.builder()
                                .id(orderId)
                                .supplierName("Proveedor ABC")
                                .status(PurchaseOrderStatus.PENDING)
                                .createdAt(OffsetDateTime.now())
                                .receivedAt(null)
                                .itemCount(3)
                                .totalAmount(new BigDecimal("5000"))
                                .build();
                when(purchaseOrderService.list(userEmail, businessId)).thenReturn(List.of(item));

                // When
                ResponseEntity<List<PurchaseOrderListItem>> response = purchaseOrderController.list(
                                businessId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).supplierName()).isEqualTo("Proveedor ABC");
                verify(purchaseOrderService).list(userEmail, businessId);
        }

        // ==================== GET BY ID ====================

        @Test
        @DisplayName("Should return purchase order by id")
        void shouldReturnPurchaseOrderById() {
                // Given
                PurchaseOrderResponse order = PurchaseOrderResponse.builder()
                                .id(orderId)
                                .supplierName("Proveedor ABC")
                                .status(PurchaseOrderStatus.PENDING)
                                .createdAt(OffsetDateTime.now())
                                .items(List.of())
                                .totalAmount(new BigDecimal("5000"))
                                .build();
                when(purchaseOrderService.getById(userEmail, businessId, orderId)).thenReturn(order);

                // When
                ResponseEntity<PurchaseOrderResponse> response = purchaseOrderController.getById(
                                businessId, orderId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().id()).isEqualTo(orderId);
                verify(purchaseOrderService).getById(userEmail, businessId, orderId);
        }

        // ==================== MARK AS RECEIVED ====================

        @Test
        @DisplayName("Should mark purchase order as received")
        @SuppressWarnings("unchecked")
        void shouldMarkAsReceived() {
                // Given
                doNothing().when(purchaseOrderService).markAsReceived(userEmail, businessId, orderId);

                // When
                ResponseEntity<?> response = purchaseOrderController.markAsReceived(
                                businessId, orderId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Orden marcada como recibida y stock actualizado");
                verify(purchaseOrderService).markAsReceived(userEmail, businessId, orderId);
        }

        // ==================== CANCEL ====================

        @Test
        @DisplayName("Should cancel purchase order")
        @SuppressWarnings("unchecked")
        void shouldCancelPurchaseOrder() {
                // Given
                doNothing().when(purchaseOrderService).cancel(userEmail, businessId, orderId);

                // When
                ResponseEntity<?> response = purchaseOrderController.cancel(businessId, orderId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Orden cancelada");
                verify(purchaseOrderService).cancel(userEmail, businessId, orderId);
        }
}
