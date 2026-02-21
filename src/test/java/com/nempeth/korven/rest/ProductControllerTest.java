package com.nempeth.korven.rest;

import com.nempeth.korven.constants.StockUnit;
import com.nempeth.korven.rest.dto.CategoryResponse;
import com.nempeth.korven.rest.dto.ProductResponse;
import com.nempeth.korven.rest.dto.ProductUpsertRequest;
import com.nempeth.korven.rest.dto.UpdateStockRequest;
import com.nempeth.korven.service.ProductService;
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
@DisplayName("ProductController Tests")
class ProductControllerTest {

        @Mock
        private ProductService productService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private ProductController productController;

        private String userEmail;
        private UUID businessId;
        private UUID productId;
        private UUID categoryId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                productId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                when(authentication.getName()).thenReturn(userEmail);
        }

        private ProductResponse createProductResponse() {
                return ProductResponse.builder()
                                .id(productId)
                                .name("Coca Cola 500ml")
                                .description("Bebida gaseosa")
                                .price(new BigDecimal("1500"))
                                .cost(new BigDecimal("800"))
                                .category(new CategoryResponse(categoryId, "Bebidas", null, null, null))
                                .stockQuantity(new BigDecimal("50"))
                                .stockUnit(StockUnit.UNIDADES)
                                .reorderPoint(new BigDecimal("10"))
                                .build();
        }

        // ==================== CREATE ====================

        @Test
        @DisplayName("Should create product and return productId")
        @SuppressWarnings("unchecked")
        void shouldCreateProduct() {
                // Given
                ProductUpsertRequest request = new ProductUpsertRequest(
                                "Coca Cola 500ml", "Bebida gaseosa",
                                new BigDecimal("1500"), new BigDecimal("800"),
                                categoryId, new BigDecimal("50"), StockUnit.UNIDADES, new BigDecimal("10"));
                when(productService.create(userEmail, businessId, request)).thenReturn(productId);

                // When
                ResponseEntity<?> response = productController.create(businessId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("productId")).isEqualTo(productId.toString());
                verify(productService).create(userEmail, businessId, request);
        }

        // ==================== LIST ====================

        @Test
        @DisplayName("Should list all products when no categoryId")
        void shouldListAllProducts_whenNoCategoryId() {
                // Given
                ProductResponse product = createProductResponse();
                when(productService.listByBusiness(userEmail, businessId)).thenReturn(List.of(product));

                // When
                ResponseEntity<List<ProductResponse>> response = productController.list(
                                businessId, null, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                verify(productService).listByBusiness(userEmail, businessId);
                verify(productService, never()).listByBusinessAndCategory(any(), any(), any());
        }

        @Test
        @DisplayName("Should list products filtered by categoryId")
        void shouldListProducts_filteredByCategoryId() {
                // Given
                ProductResponse product = createProductResponse();
                when(productService.listByBusinessAndCategory(userEmail, businessId, categoryId))
                                .thenReturn(List.of(product));

                // When
                ResponseEntity<List<ProductResponse>> response = productController.list(
                                businessId, categoryId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                verify(productService).listByBusinessAndCategory(userEmail, businessId, categoryId);
                verify(productService, never()).listByBusiness(any(), any());
        }

        // ==================== UPDATE ====================

        @Test
        @DisplayName("Should update product successfully")
        @SuppressWarnings("unchecked")
        void shouldUpdateProduct() {
                // Given
                ProductUpsertRequest request = new ProductUpsertRequest(
                                "Coca Cola 1L", "Bebida gaseosa grande",
                                new BigDecimal("2500"), new BigDecimal("1200"),
                                categoryId, new BigDecimal("30"), StockUnit.UNIDADES, new BigDecimal("5"));
                doNothing().when(productService).update(userEmail, businessId, productId, request);

                // When
                ResponseEntity<?> response = productController.update(businessId, productId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Producto actualizado");
                verify(productService).update(userEmail, businessId, productId, request);
        }

        // ==================== UPDATE STOCK ====================

        @Test
        @DisplayName("Should update stock successfully")
        @SuppressWarnings("unchecked")
        void shouldUpdateStock() {
                // Given
                UpdateStockRequest request = new UpdateStockRequest(
                                new BigDecimal("100"), StockUnit.UNIDADES, new BigDecimal("15"));
                doNothing().when(productService).updateStock(userEmail, businessId, productId, request);

                // When
                ResponseEntity<?> response = productController.updateStock(
                                businessId, productId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Stock actualizado");
                verify(productService).updateStock(userEmail, businessId, productId, request);
        }

        // ==================== DELETE ====================

        @Test
        @DisplayName("Should delete product successfully")
        @SuppressWarnings("unchecked")
        void shouldDeleteProduct() {
                // Given
                doNothing().when(productService).delete(userEmail, businessId, productId);

                // When
                ResponseEntity<?> response = productController.delete(businessId, productId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Producto eliminado");
                verify(productService).delete(userEmail, businessId, productId);
        }
}
