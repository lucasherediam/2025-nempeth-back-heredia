package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.BusinessRankingResponse;
import com.nempeth.korven.rest.dto.EmployeeRankingResponse;
import com.nempeth.korven.service.RankingService;
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
@DisplayName("RankingController Tests")
class RankingControllerTest {

        @Mock
        private RankingService rankingService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private RankingController rankingController;

        private String userEmail;
        private UUID businessId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                when(authentication.getName()).thenReturn(userEmail);
        }

        // ==================== EMPLOYEE RANKINGS ====================

        @Test
        @DisplayName("Should return employee rankings")
        void shouldReturnEmployeeRankings() {
                // Given
                EmployeeRankingResponse ranking = new EmployeeRankingResponse(
                                "John", "Doe", 15L, new BigDecimal("45000"), 1, true);
                when(rankingService.getEmployeeRankings(userEmail, businessId)).thenReturn(List.of(ranking));

                // When
                ResponseEntity<List<EmployeeRankingResponse>> response = rankingController
                                .getEmployeeRankings(businessId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).name()).isEqualTo("John");
                assertThat(response.getBody().get(0).position()).isEqualTo(1);
                verify(rankingService).getEmployeeRankings(userEmail, businessId);
        }

        // ==================== BUSINESS RANKINGS ====================

        @Test
        @DisplayName("Should return business rankings")
        void shouldReturnBusinessRankings() {
                // Given
                BusinessRankingResponse ranking = new BusinessRankingResponse(
                                businessId, "Mi Negocio", new BigDecimal("85.5"), 1, true);
                when(rankingService.getBusinessRankings(userEmail)).thenReturn(List.of(ranking));

                // When
                ResponseEntity<List<BusinessRankingResponse>> response = rankingController
                                .getBusinessRankings(businessId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).businessName()).isEqualTo("Mi Negocio");
                assertThat(response.getBody().get(0).position()).isEqualTo(1);
                verify(rankingService).getBusinessRankings(userEmail);
        }
}
