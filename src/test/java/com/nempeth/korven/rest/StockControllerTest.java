package com.nempeth.korven.rest;

import com.nempeth.korven.constants.StockStatus;
import com.nempeth.korven.constants.StockUnit;
import com.nempeth.korven.rest.dto.StockItemResponse;
import com.nempeth.korven.service.StockService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockController Tests")
class StockControllerTest {

        @Mock
        private StockService stockService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private StockController stockController;

        private String userEmail;
        private UUID businessId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                when(authentication.getName()).thenReturn(userEmail);
        }

        @Test
        @DisplayName("Should return business stock")
        void shouldReturnBusinessStock() {
                // Given
                StockItemResponse item = StockItemResponse.builder()
                                .productId(UUID.randomUUID())
                                .productName("Coca Cola 500ml")
                                .categoryId(UUID.randomUUID())
                                .categoryName("Bebidas")
                                .stockQuantity(new BigDecimal("50"))
                                .stockUnit(StockUnit.UNIDADES)
                                .reorderPoint(new BigDecimal("10"))
                                .status(StockStatus.OK)
                                .build();
                when(stockService.getBusinessStock(userEmail, businessId)).thenReturn(List.of(item));

                // When
                ResponseEntity<List<StockItemResponse>> response = stockController.getBusinessStock(
                                businessId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).productName()).isEqualTo("Coca Cola 500ml");
                verify(stockService).getBusinessStock(userEmail, businessId);
        }
}
