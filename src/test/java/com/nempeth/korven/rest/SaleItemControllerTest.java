package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.CreateSaleItemRequest;
import com.nempeth.korven.rest.dto.SaleItemResponse;
import com.nempeth.korven.service.SaleItemService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleItemController Tests")
class SaleItemControllerTest {

        @Mock
        private SaleItemService saleItemService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private SaleItemController saleItemController;

        private String userEmail;
        private UUID businessId;
        private UUID saleId;
        private UUID itemId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                saleId = UUID.randomUUID();
                itemId = UUID.randomUUID();
                when(authentication.getName()).thenReturn(userEmail);
        }

        // ==================== ADD ITEM TO SALE ====================

        @Test
        @DisplayName("Should add item to sale and return itemId")
        @SuppressWarnings("unchecked")
        void shouldAddItemToSale() {
                // Given
                UUID productId = UUID.randomUUID();
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 3);
                when(saleItemService.addItemToSale(userEmail, businessId, saleId, request)).thenReturn(itemId);

                // When
                ResponseEntity<?> response = saleItemController.addItemToSale(
                                businessId, saleId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Item agregado exitosamente");
                assertThat(body.get("itemId")).isEqualTo(itemId.toString());
                verify(saleItemService).addItemToSale(userEmail, businessId, saleId, request);
        }

        // ==================== GET SALE ITEMS ====================

        @Test
        @DisplayName("Should return sale items")
        void shouldReturnSaleItems() {
                // Given
                SaleItemResponse item = SaleItemResponse.builder()
                                .id(itemId)
                                .categoryName("Bebidas")
                                .productName("Coca Cola 500ml")
                                .quantity(3)
                                .unitPrice(new BigDecimal("1500"))
                                .unitCost(new BigDecimal("800"))
                                .lineTotal(new BigDecimal("4500"))
                                .build();
                when(saleItemService.getSaleItems(userEmail, businessId, saleId)).thenReturn(List.of(item));

                // When
                ResponseEntity<List<SaleItemResponse>> response = saleItemController.getSaleItems(
                                businessId, saleId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).productName()).isEqualTo("Coca Cola 500ml");
                verify(saleItemService).getSaleItems(userEmail, businessId, saleId);
        }
}
