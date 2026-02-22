package com.nempeth.korven.rest;

import com.nempeth.korven.rest.dto.*;
import com.nempeth.korven.service.AuthService;
import com.nempeth.korven.service.PasswordResetService;
import com.nempeth.korven.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

        @Mock
        private AuthService authService;

        @Mock
        private PasswordResetService passwordResetService;

        @Mock
        private RateLimiterService rateLimiterService;

        @Mock
        private HttpServletRequest httpServletRequest;

        @InjectMocks
        private AuthController authController;

        private UUID userId;

        @BeforeEach
        void setUp() {
                userId = UUID.randomUUID();
        }

        // ==================== REGISTER ====================

        @Test
        @DisplayName("Should register user and return userId")
        @SuppressWarnings("unchecked")
        void shouldRegisterUser() {
                // Given
                RegisterRequest request = new RegisterRequest("test@example.com", "John", "Doe", "pass123");
                when(authService.register(request)).thenReturn(userId);

                // When
                ResponseEntity<?> response = authController.register(request);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("userId")).isEqualTo(userId.toString());
                verify(authService).register(request);
        }

        // ==================== REGISTER OWNER ====================

        @Test
        @DisplayName("Should register owner and return registration response")
        void shouldRegisterOwner() {
                // Given
                RegisterOwnerRequest request = new RegisterOwnerRequest(
                                "owner@example.com", "Jane", "Smith", "pass123", "Mi Negocio");
                RegistrationResponse regResponse = RegistrationResponse.builder()
                                .userId(userId)
                                .message("Registro exitoso")
                                .business(null)
                                .build();
                when(authService.registerOwner(request)).thenReturn(regResponse);

                // When
                ResponseEntity<RegistrationResponse> response = authController.registerOwner(request);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().userId()).isEqualTo(userId);
                verify(authService).registerOwner(request);
        }

        // ==================== REGISTER EMPLOYEE ====================

        @Test
        @DisplayName("Should register employee and return registration response")
        void shouldRegisterEmployee() {
                // Given
                RegisterEmployeeRequest request = new RegisterEmployeeRequest(
                                "employee@example.com", "Bob", "Jones", "pass123", "ABC12345");
                RegistrationResponse regResponse = RegistrationResponse.builder()
                                .userId(userId)
                                .message("Registro exitoso")
                                .business(null)
                                .build();
                when(authService.registerEmployee(request)).thenReturn(regResponse);

                // When
                ResponseEntity<RegistrationResponse> response = authController.registerEmployee(request);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().userId()).isEqualTo(userId);
                verify(authService).registerEmployee(request);
        }

        // ==================== LOGIN ====================

        @Test
        @DisplayName("Should login and return token")
        @SuppressWarnings("unchecked")
        void shouldLoginAndReturnToken() {
                // Given
                LoginRequest request = new LoginRequest("test@example.com", "pass123");
                when(authService.loginAndIssueToken(request)).thenReturn("jwt-token-123");

                // When
                ResponseEntity<?> response = authController.login(request, httpServletRequest);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("token")).isEqualTo("jwt-token-123");
                assertThat(body.get("message")).isEqualTo("Login exitoso");
                verify(authService).loginAndIssueToken(request);
        }

        // ==================== FORGOT PASSWORD ====================

        @Test
        @DisplayName("Should initiate password reset and return 200")
        void shouldInitiatePasswordReset() {
                // Given
                ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
                when(httpServletRequest.getHeader("Origin")).thenReturn("https://korven.com.ar");
                doNothing().when(passwordResetService).startReset("test@example.com", httpServletRequest);

                // When
                ResponseEntity<Void> response = authController.forgotPassword(request, httpServletRequest);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                verify(passwordResetService).startReset("test@example.com", httpServletRequest);
        }

        // ==================== VALIDATE TOKEN ====================

        @Test
        @DisplayName("Should return 200 when token is valid")
        void shouldReturn200_whenTokenIsValid() {
                // Given
                when(passwordResetService.validateToken("valid-token")).thenReturn(true);

                // When
                ResponseEntity<Void> response = authController.validate("valid-token");

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                verify(passwordResetService).validateToken("valid-token");
        }

        @Test
        @DisplayName("Should return 410 GONE when token is invalid or expired")
        void shouldReturn410_whenTokenIsInvalid() {
                // Given
                when(passwordResetService.validateToken("expired-token")).thenReturn(false);

                // When
                ResponseEntity<Void> response = authController.validate("expired-token");

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
                verify(passwordResetService).validateToken("expired-token");
        }

        // ==================== RESET PASSWORD ====================

        @Test
        @DisplayName("Should reset password and return 204")
        void shouldResetPasswordAndReturn204() {
                // Given
                ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "newPass456");
                doNothing().when(passwordResetService).resetPassword("valid-token", "newPass456");
                when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

                // When
                ResponseEntity<Void> response = authController.reset(request, httpServletRequest);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                verify(passwordResetService).resetPassword("valid-token", "newPass456");
        }
}
