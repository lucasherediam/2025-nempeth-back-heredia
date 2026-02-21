package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.*;
import com.nempeth.korven.service.GoalService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalController Tests")
class GoalControllerTest {

        @Mock
        private GoalService goalService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private GoalController goalController;

        private String userEmail;
        private UUID businessId;
        private UUID goalId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                goalId = UUID.randomUUID();
        }

        private void setupAuth() {
                when(authentication.getName()).thenReturn(userEmail);
        }

        private GoalResponse createGoalResponse() {
                return new GoalResponse(
                                goalId,
                                businessId,
                                "Meta Enero",
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 31),
                                false,
                                false,
                                List.of(new GoalCategoryTargetResponse(
                                                UUID.randomUUID(),
                                                UUID.randomUUID(),
                                                "Bebidas",
                                                new BigDecimal("10000"),
                                                new BigDecimal("5000"),
                                                new BigDecimal("50.00"))));
        }

        // ==================== GET ALL GOALS ====================

        @Test
        @DisplayName("Should return all goals for a business")
        void shouldReturnAllGoals() {
                // Given
                setupAuth();
                GoalResponse goal = createGoalResponse();
                when(goalService.getAllGoalsByBusiness(userEmail, businessId)).thenReturn(List.of(goal));

                // When
                ResponseEntity<List<GoalResponse>> response = goalController.getAllGoals(businessId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).name()).isEqualTo("Meta Enero");
                verify(goalService).getAllGoalsByBusiness(userEmail, businessId);
        }

        // ==================== GET GOAL BY ID ====================

        @Test
        @DisplayName("Should return goal by id")
        void shouldReturnGoalById() {
                // Given
                setupAuth();
                GoalResponse goal = createGoalResponse();
                when(goalService.getGoalById(userEmail, businessId, goalId)).thenReturn(goal);

                // When
                ResponseEntity<GoalResponse> response = goalController.getGoalById(businessId, goalId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().id()).isEqualTo(goalId);
                verify(goalService).getGoalById(userEmail, businessId, goalId);
        }

        // ==================== GET HISTORICAL REPORT ====================

        @Test
        @DisplayName("Should return historical report")
        void shouldReturnHistoricalReport() {
                // Given
                setupAuth();
                GoalReportResponse report = new GoalReportResponse(
                                goalId, "Meta Enero",
                                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                                new BigDecimal("50000"), new BigDecimal("35000"),
                                new BigDecimal("70.00"), List.of());
                when(goalService.getHistoricalReport(userEmail, businessId)).thenReturn(List.of(report));

                // When
                ResponseEntity<List<GoalReportResponse>> response = goalController.getHistoricalReport(businessId,
                                authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).name()).isEqualTo("Meta Enero");
                verify(goalService).getHistoricalReport(userEmail, businessId);
        }

        // ==================== GET GOALS SUMMARY ====================

        @Test
        @DisplayName("Should return goals summary")
        void shouldReturnGoalsSummary() {
                // Given
                setupAuth();
                ActiveGoalSummaryResponse summary = new ActiveGoalSummaryResponse(
                                goalId, "Meta Enero",
                                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                                "15 días", 2, 5, "EN PROGRESO",
                                new BigDecimal("50000"), new BigDecimal("20000"));
                when(goalService.getGoalsSummary(userEmail, businessId)).thenReturn(List.of(summary));

                // When
                ResponseEntity<List<ActiveGoalSummaryResponse>> response = goalController.getGoalsSummary(businessId,
                                authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).daysRemaining()).isEqualTo("15 días");
                verify(goalService).getGoalsSummary(userEmail, businessId);
        }

        // ==================== GET GOAL REPORT ====================

        @Test
        @DisplayName("Should return goal report")
        void shouldReturnGoalReport() {
                // Given
                setupAuth();
                GoalReportResponse report = new GoalReportResponse(
                                goalId, "Meta Enero",
                                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                                new BigDecimal("50000"), new BigDecimal("35000"),
                                new BigDecimal("70.00"), List.of());
                when(goalService.getGoalReport(userEmail, businessId, goalId)).thenReturn(report);

                // When
                ResponseEntity<GoalReportResponse> response = goalController.getGoalReport(businessId, goalId,
                                authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().totalAchievement()).isEqualByComparingTo(new BigDecimal("70.00"));
                verify(goalService).getGoalReport(userEmail, businessId, goalId);
        }

        // ==================== CREATE GOAL ====================

        @Test
        @DisplayName("Should create goal and return 201")
        @SuppressWarnings("unchecked")
        void shouldCreateGoalAndReturn201() {
                // Given
                setupAuth();
                UUID categoryId = UUID.randomUUID();
                CreateGoalRequest request = new CreateGoalRequest(
                                "Meta Febrero",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 28),
                                new BigDecimal("60000"),
                                List.of(new CategoryTargetRequest(categoryId, new BigDecimal("30000"))));

                GoalResponse created = new GoalResponse(
                                UUID.randomUUID(), businessId, "Meta Febrero",
                                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28),
                                false, false, List.of());
                when(goalService.createGoal(userEmail, businessId, request)).thenReturn(created);

                // When
                ResponseEntity<?> response = goalController.createGoal(businessId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body).containsKey("message");
                assertThat(body).containsKey("goal");
                assertThat(body.get("message")).isEqualTo("Meta creada exitosamente");
                assertThat(body.get("goal")).isEqualTo(created);
                verify(goalService).createGoal(userEmail, businessId, request);
        }

        // ==================== UPDATE GOAL ====================

        @Test
        @DisplayName("Should update goal and return 200")
        @SuppressWarnings("unchecked")
        void shouldUpdateGoalAndReturn200() {
                // Given
                setupAuth();
                UUID categoryId = UUID.randomUUID();
                UpdateGoalRequest request = new UpdateGoalRequest(
                                "Meta Enero Actualizada",
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 31),
                                new BigDecimal("55000"),
                                List.of(new CategoryTargetRequest(categoryId, new BigDecimal("25000"))));

                GoalResponse updated = new GoalResponse(
                                goalId, businessId, "Meta Enero Actualizada",
                                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                                false, false, List.of());
                when(goalService.updateGoal(userEmail, businessId, goalId, request)).thenReturn(updated);

                // When
                ResponseEntity<?> response = goalController.updateGoal(businessId, goalId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Meta actualizada exitosamente");
                assertThat(body.get("goal")).isEqualTo(updated);
                verify(goalService).updateGoal(userEmail, businessId, goalId, request);
        }

        // ==================== DELETE GOAL ====================

        @Test
        @DisplayName("Should delete goal and return 200")
        @SuppressWarnings("unchecked")
        void shouldDeleteGoalAndReturn200() {
                // Given
                setupAuth();
                doNothing().when(goalService).deleteGoal(userEmail, businessId, goalId);

                // When
                ResponseEntity<?> response = goalController.deleteGoal(businessId, goalId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Meta eliminada exitosamente");
                verify(goalService).deleteGoal(userEmail, businessId, goalId);
        }
}
