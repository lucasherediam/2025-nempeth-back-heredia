package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.MonthlyCategoryProfitResponse;
import com.nempeth.korven.rest.dto.MonthlyCategoryRevenueResponse;
import com.nempeth.korven.rest.dto.MonthlyProfitResponse;
import com.nempeth.korven.rest.dto.MonthlyRevenueResponse;
import com.nempeth.korven.service.AnalyticsService;
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
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsController Tests")
class AnalyticsControllerTest {

        @Mock
        private AnalyticsService analyticsService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private AnalyticsController analyticsController;

        private String userEmail;
        private UUID businessId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                when(authentication.getName()).thenReturn(userEmail);
        }

        // ==================== MONTHLY REVENUE BY CATEGORY ====================

        @Test
        @DisplayName("Should return monthly revenue by category")
        void shouldReturnMonthlyRevenueByCategory() {
                // Given
                MonthlyCategoryRevenueResponse item = MonthlyCategoryRevenueResponse.builder()
                                .month(YearMonth.of(2026, 1))
                                .categoryName("Bebidas")
                                .revenue(new BigDecimal("25000"))
                                .build();
                when(analyticsService.getMonthlyRevenueByCategory(userEmail, businessId, 2026))
                                .thenReturn(List.of(item));

                // When
                ResponseEntity<List<MonthlyCategoryRevenueResponse>> response = analyticsController
                                .getMonthlyRevenueByCategory(businessId, 2026, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).categoryName()).isEqualTo("Bebidas");
                verify(analyticsService).getMonthlyRevenueByCategory(userEmail, businessId, 2026);
        }

        // ==================== MONTHLY PROFIT BY CATEGORY ====================

        @Test
        @DisplayName("Should return monthly profit by category")
        void shouldReturnMonthlyProfitByCategory() {
                // Given
                MonthlyCategoryProfitResponse item = MonthlyCategoryProfitResponse.builder()
                                .month(YearMonth.of(2026, 1))
                                .categoryName("Bebidas")
                                .profit(new BigDecimal("12000"))
                                .build();
                when(analyticsService.getMonthlyProfitByCategory(userEmail, businessId, 2026))
                                .thenReturn(List.of(item));

                // When
                ResponseEntity<List<MonthlyCategoryProfitResponse>> response = analyticsController
                                .getMonthlyProfitByCategory(businessId, 2026, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).profit()).isEqualByComparingTo(new BigDecimal("12000"));
                verify(analyticsService).getMonthlyProfitByCategory(userEmail, businessId, 2026);
        }

        // ==================== MONTHLY TOTAL REVENUE ====================

        @Test
        @DisplayName("Should return monthly total revenue")
        void shouldReturnMonthlyTotalRevenue() {
                // Given
                MonthlyRevenueResponse item = MonthlyRevenueResponse.builder()
                                .month(YearMonth.of(2026, 1))
                                .revenue(new BigDecimal("50000"))
                                .build();
                when(analyticsService.getMonthlyTotalRevenue(userEmail, businessId, 2026))
                                .thenReturn(List.of(item));

                // When
                ResponseEntity<List<MonthlyRevenueResponse>> response = analyticsController
                                .getMonthlyTotalRevenue(businessId, 2026, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).revenue()).isEqualByComparingTo(new BigDecimal("50000"));
                verify(analyticsService).getMonthlyTotalRevenue(userEmail, businessId, 2026);
        }

        // ==================== MONTHLY TOTAL PROFIT ====================

        @Test
        @DisplayName("Should return monthly total profit")
        void shouldReturnMonthlyTotalProfit() {
                // Given
                MonthlyProfitResponse item = MonthlyProfitResponse.builder()
                                .month(YearMonth.of(2026, 1))
                                .profit(new BigDecimal("20000"))
                                .build();
                when(analyticsService.getMonthlyTotalProfit(userEmail, businessId, 2026))
                                .thenReturn(List.of(item));

                // When
                ResponseEntity<List<MonthlyProfitResponse>> response = analyticsController
                                .getMonthlyTotalProfit(businessId, 2026, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).profit()).isEqualByComparingTo(new BigDecimal("20000"));
                verify(analyticsService).getMonthlyTotalProfit(userEmail, businessId, 2026);
        }
}
