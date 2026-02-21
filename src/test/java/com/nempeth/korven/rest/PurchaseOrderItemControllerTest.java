package com.nempeth.korven.rest;

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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderItemController Tests")
class PurchaseOrderItemControllerTest {

        @Mock
        private PurchaseOrderService purchaseOrderService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private PurchaseOrderItemController purchaseOrderItemController;

        private String userEmail;
        private UUID businessId;
        private UUID purchaseOrderId;
        private UUID itemId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                purchaseOrderId = UUID.randomUUID();
                itemId = UUID.randomUUID();
                when(authentication.getName()).thenReturn(userEmail);
        }

        @Test
        @DisplayName("Should delete item from purchase order")
        @SuppressWarnings("unchecked")
        void shouldDeleteItem() {
                // Given
                doNothing().when(purchaseOrderService).deleteItem(userEmail, businessId, purchaseOrderId, itemId);

                // When
                ResponseEntity<?> response = purchaseOrderItemController.deleteItem(
                                businessId, purchaseOrderId, itemId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Item eliminado");
                verify(purchaseOrderService).deleteItem(userEmail, businessId, purchaseOrderId, itemId);
        }
}
