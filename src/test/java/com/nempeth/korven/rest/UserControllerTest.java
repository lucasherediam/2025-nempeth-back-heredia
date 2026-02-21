package com.nempeth.korven.rest;

import com.nempeth.korven.constants.MembershipRole;
import com.nempeth.korven.constants.MembershipStatus;
import com.nempeth.korven.rest.dto.*;
import com.nempeth.korven.service.UserService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

        @Mock
        private UserService userService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private UserController userController;

        private String userEmail;
        private UUID userId;
        private UUID businessId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                userId = UUID.randomUUID();
                businessId = UUID.randomUUID();
        }

        private void setupAuth() {
                when(authentication.getName()).thenReturn(userEmail);
        }

        private UserResponse createUserResponse() {
                return UserResponse.builder()
                                .id(userId)
                                .email(userEmail)
                                .name("John")
                                .lastName("Doe")
                                .businesses(List.of())
                                .build();
        }

        // ==================== GET CURRENT USER ====================

        @Test
        @DisplayName("Should return current user")
        void shouldReturnCurrentUser() {
                // Given
                setupAuth();
                UserResponse userResponse = createUserResponse();
                when(userService.getUserByEmail(userEmail)).thenReturn(userResponse);

                // When
                ResponseEntity<UserResponse> response = userController.getCurrentUser(authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().email()).isEqualTo(userEmail);
                verify(userService).getUserByEmail(userEmail);
        }

        // ==================== GET USER BY ID ====================

        @Test
        @DisplayName("Should return user by id")
        void shouldReturnUserById() {
                // Given
                setupAuth();
                UserResponse userResponse = createUserResponse();
                when(userService.getUserById(userId, userEmail)).thenReturn(userResponse);

                // When
                ResponseEntity<UserResponse> response = userController.getUserById(userId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().id()).isEqualTo(userId);
                verify(userService).getUserById(userId, userEmail);
        }

        // ==================== UPDATE PROFILE ====================

        @Test
        @DisplayName("Should return emailChanged true when email was updated")
        @SuppressWarnings("unchecked")
        void shouldReturnEmailChangedTrue_whenEmailUpdated() {
                // Given
                setupAuth();
                UpdateUserProfileRequest request = new UpdateUserProfileRequest("new@example.com", "John", "Doe");
                when(userService.updateUserProfile(userId, userEmail, request)).thenReturn(true);

                // When
                ResponseEntity<?> response = userController.updateProfile(userId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("emailChanged")).isEqualTo(true);
                assertThat(body.get("message").toString()).contains("Reingresá con el nuevo email");
                verify(userService).updateUserProfile(userId, userEmail, request);
        }

        @Test
        @DisplayName("Should return emailChanged false when email was not updated")
        @SuppressWarnings("unchecked")
        void shouldReturnEmailChangedFalse_whenEmailNotUpdated() {
                // Given
                setupAuth();
                UpdateUserProfileRequest request = new UpdateUserProfileRequest(userEmail, "Jane", "Smith");
                when(userService.updateUserProfile(userId, userEmail, request)).thenReturn(false);

                // When
                ResponseEntity<?> response = userController.updateProfile(userId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("emailChanged")).isEqualTo(false);
                assertThat(body.get("message")).isEqualTo("Usuario actualizado");
                verify(userService).updateUserProfile(userId, userEmail, request);
        }

        // ==================== UPDATE PASSWORD ====================

        @Test
        @DisplayName("Should update password successfully")
        @SuppressWarnings("unchecked")
        void shouldUpdatePasswordSuccessfully() {
                // Given
                setupAuth();
                UpdateUserPasswordRequest request = new UpdateUserPasswordRequest("oldPass123", "newPass456");
                doNothing().when(userService).updateUserPassword(userId, userEmail, request);

                // When
                ResponseEntity<?> response = userController.updatePassword(userId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Contraseña actualizada");
                verify(userService).updateUserPassword(userId, userEmail, request);
        }

        // ==================== UPDATE MEMBERSHIP STATUS ====================

        @Test
        @DisplayName("Should update membership status successfully")
        @SuppressWarnings("unchecked")
        void shouldUpdateMembershipStatusSuccessfully() {
                // Given
                setupAuth();
                UpdateMembershipStatusRequest request = new UpdateMembershipStatusRequest(MembershipStatus.ACTIVE);
                doNothing().when(userService).updateMembershipStatus(businessId, userId, userEmail, request);

                // When
                ResponseEntity<?> response = userController.updateMembershipStatus(businessId, userId, request,
                                authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Status de membresía actualizado");
                verify(userService).updateMembershipStatus(businessId, userId, userEmail, request);
        }

        // ==================== UPDATE MEMBERSHIP ROLE ====================

        @Test
        @DisplayName("Should update membership role successfully")
        @SuppressWarnings("unchecked")
        void shouldUpdateMembershipRoleSuccessfully() {
                // Given
                setupAuth();
                UpdateMembershipRoleRequest request = new UpdateMembershipRoleRequest(MembershipRole.OWNER);
                doNothing().when(userService).updateMembershipRole(businessId, userId, userEmail, request);

                // When
                ResponseEntity<?> response = userController.updateMembershipRole(businessId, userId, request,
                                authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Role de membresía actualizado");
                verify(userService).updateMembershipRole(businessId, userId, userEmail, request);
        }

        // ==================== DELETE USER ====================

        @Test
        @DisplayName("Should delete user successfully")
        @SuppressWarnings("unchecked")
        void shouldDeleteUserSuccessfully() {
                // Given
                setupAuth();
                doNothing().when(userService).deleteUser(userId, userEmail);

                // When
                ResponseEntity<?> response = userController.delete(userId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Usuario eliminado");
                verify(userService).deleteUser(userId, userEmail);
        }
}
