package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.ExternalProductResponse;
import com.nempeth.korven.service.ExternalProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalProductController Tests")
class ExternalProductControllerTest {

        @Mock
        private ExternalProductService externalProductService;

        @InjectMocks
        private ExternalProductController externalProductController;

        @Test
        @DisplayName("Should return all products")
        void shouldReturnAllProducts() {
                // Given
                ExternalProductResponse product = ExternalProductResponse.builder()
                                .name("Coca Cola 500ml")
                                .build();
                when(externalProductService.getAllProducts()).thenReturn(List.of(product));

                // When
                ResponseEntity<List<ExternalProductResponse>> response = externalProductController.getAllProducts();

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).getName()).isEqualTo("Coca Cola 500ml");
                verify(externalProductService).getAllProducts();
        }
}
