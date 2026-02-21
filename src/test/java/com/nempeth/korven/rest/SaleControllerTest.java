package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.CreateSaleRequest;
import com.nempeth.korven.rest.dto.SaleResponse;
import com.nempeth.korven.rest.dto.UpdateSaleRequest;
import com.nempeth.korven.service.SaleService;
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
@DisplayName("SaleController Tests")
class SaleControllerTest {

        @Mock
        private SaleService saleService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private SaleController saleController;

        private String userEmail;
        private UUID businessId;
        private UUID saleId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                saleId = UUID.randomUUID();
                when(authentication.getName()).thenReturn(userEmail);
        }

        private SaleResponse createSaleResponse() {
                return SaleResponse.builder()
                                .id(saleId)
                                .orderNumber(1)
                                .note("Mesa 5")
                                .occurredAt(OffsetDateTime.now())
                                .totalAmount(new BigDecimal("250.00"))
                                .createdByUserName("John Doe")
                                .items(List.of())
                                .build();
        }

        // ==================== CREATE SALE ====================

        @Test
        @DisplayName("Should create sale with note when request is provided")
        @SuppressWarnings("unchecked")
        void shouldCreateSaleWithNote() {
                // Given
                CreateSaleRequest request = new CreateSaleRequest("Mesa 5");
                when(saleService.createSale(userEmail, businessId, "Mesa 5")).thenReturn(saleId);

                // When
                ResponseEntity<?> response = saleController.createSale(businessId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Venta creada exitosamente");
                assertThat(body.get("saleId")).isEqualTo(saleId.toString());
                verify(saleService).createSale(userEmail, businessId, "Mesa 5");
        }

        @Test
        @DisplayName("Should create sale with null note when request is null")
        @SuppressWarnings("unchecked")
        void shouldCreateSaleWithNullNote_whenRequestIsNull() {
                // Given
                when(saleService.createSale(userEmail, businessId, null)).thenReturn(saleId);

                // When
                ResponseEntity<?> response = saleController.createSale(businessId, null, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("saleId")).isEqualTo(saleId.toString());
                verify(saleService).createSale(userEmail, businessId, null);
        }

        // ==================== GET SALES ====================

        @Test
        @DisplayName("Should return sales by date range when dates provided")
        void shouldReturnSalesByDateRange() {
                // Given
                OffsetDateTime startDate = OffsetDateTime.now().minusDays(7);
                OffsetDateTime endDate = OffsetDateTime.now();
                SaleResponse sale = createSaleResponse();
                when(saleService.getSalesByBusinessAndDateRange(userEmail, businessId, startDate, endDate))
                                .thenReturn(List.of(sale));

                // When
                ResponseEntity<List<SaleResponse>> response = saleController.getSales(
                                businessId, startDate, endDate, null, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                verify(saleService).getSalesByBusinessAndDateRange(userEmail, businessId, startDate, endDate);
                verify(saleService, never()).getSalesByBusiness(any(), any(), any());
        }

        @Test
        @DisplayName("Should return sales by open filter when no dates provided")
        void shouldReturnSalesByOpenFilter_whenNoDates() {
                // Given
                SaleResponse sale = createSaleResponse();
                when(saleService.getSalesByBusiness(userEmail, businessId, true))
                                .thenReturn(List.of(sale));

                // When
                ResponseEntity<List<SaleResponse>> response = saleController.getSales(
                                businessId, null, null, true, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                verify(saleService).getSalesByBusiness(userEmail, businessId, true);
                verify(saleService, never()).getSalesByBusinessAndDateRange(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should return all sales when no filters provided")
        void shouldReturnAllSales_whenNoFilters() {
                // Given
                when(saleService.getSalesByBusiness(userEmail, businessId, null))
                                .thenReturn(List.of());

                // When
                ResponseEntity<List<SaleResponse>> response = saleController.getSales(
                                businessId, null, null, null, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEmpty();
                verify(saleService).getSalesByBusiness(userEmail, businessId, null);
        }

        // ==================== CLOSE SALE ====================

        @Test
        @DisplayName("Should close sale successfully")
        @SuppressWarnings("unchecked")
        void shouldCloseSaleSuccessfully() {
                // Given
                doNothing().when(saleService).closeSale(userEmail, businessId, saleId);

                // When
                ResponseEntity<?> response = saleController.closeSale(businessId, saleId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Venta cerrada exitosamente");
                verify(saleService).closeSale(userEmail, businessId, saleId);
        }

        // ==================== UPDATE SALE ====================

        @Test
        @DisplayName("Should update sale note successfully")
        void shouldUpdateSaleNoteSuccessfully() {
                // Given
                UpdateSaleRequest request = new UpdateSaleRequest("Mesa 10");
                SaleResponse updated = createSaleResponse();
                when(saleService.updateSale(userEmail, businessId, saleId, "Mesa 10")).thenReturn(updated);

                // When
                ResponseEntity<SaleResponse> response = saleController.updateSale(
                                businessId, saleId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().id()).isEqualTo(saleId);
                verify(saleService).updateSale(userEmail, businessId, saleId, "Mesa 10");
        }

        // ==================== DELETE SALE ====================

        @Test
        @DisplayName("Should delete sale successfully")
        @SuppressWarnings("unchecked")
        void shouldDeleteSaleSuccessfully() {
                // Given
                doNothing().when(saleService).deleteSale(userEmail, businessId, saleId);

                // When
                ResponseEntity<?> response = saleController.deleteSale(businessId, saleId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Venta eliminada exitosamente");
                verify(saleService).deleteSale(userEmail, businessId, saleId);
        }

        // ==================== GET SALE BY ID ====================

        @Test
        @DisplayName("Should return sale by id")
        void shouldReturnSaleById() {
                // Given
                SaleResponse sale = createSaleResponse();
                when(saleService.getSaleById(userEmail, businessId, saleId)).thenReturn(sale);

                // When
                ResponseEntity<SaleResponse> response = saleController.getSaleById(
                                businessId, saleId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().id()).isEqualTo(saleId);
                verify(saleService).getSaleById(userEmail, businessId, saleId);
        }
}
