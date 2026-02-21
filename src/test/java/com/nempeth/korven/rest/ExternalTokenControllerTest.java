package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.ExternalTokenRequest;
import com.nempeth.korven.rest.dto.ExternalTokenResponse;
import com.nempeth.korven.service.ExternalTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalTokenController Tests")
class ExternalTokenControllerTest {

        @Mock
        private ExternalTokenService externalTokenService;

        @InjectMocks
        private ExternalTokenController externalTokenController;

        @Test
        @DisplayName("Should generate token successfully")
        void shouldGenerateToken() {
                // Given
                ExternalTokenRequest request = ExternalTokenRequest.builder()
                                .clientName("test-client")
                                .build();
                ExternalTokenResponse expectedResponse = ExternalTokenResponse.builder()
                                .token("jwt-token-abc123")
                                .expiresAt("2026-03-21T15:00:00Z")
                                .message("Token generado exitosamente")
                                .build();
                when(externalTokenService.generateToken(request)).thenReturn(expectedResponse);

                // When
                ResponseEntity<ExternalTokenResponse> response = externalTokenController.generateToken(request);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getToken()).isEqualTo("jwt-token-abc123");
                assertThat(response.getBody().getMessage()).isEqualTo("Token generado exitosamente");
                verify(externalTokenService).generateToken(request);
        }
}
