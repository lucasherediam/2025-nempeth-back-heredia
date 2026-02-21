package com.nempeth.korven.rest;

import com.nempeth.korven.constants.MembershipRole;
import com.nempeth.korven.constants.MembershipStatus;
import com.nempeth.korven.rest.dto.*;
import com.nempeth.korven.service.BusinessService;
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
@DisplayName("BusinessController Tests")
class BusinessControllerTest {

        @Mock
        private BusinessService businessService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private BusinessController businessController;

        private String userEmail;
        private UUID businessId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                when(authentication.getName()).thenReturn(userEmail);
        }

        private BusinessResponse createBusinessResponse() {
                return BusinessResponse.builder()
                                .id(businessId)
                                .name("Mi Negocio")
                                .joinCode("ABC12345")
                                .joinCodeEnabled(true)
                                .build();
        }

        private BusinessMemberDetailResponse createMemberResponse(UUID userId, MembershipRole role) {
                return BusinessMemberDetailResponse.builder()
                                .userId(userId)
                                .userEmail("member@example.com")
                                .userName("John")
                                .userLastName("Doe")
                                .role(role)
                                .status(MembershipStatus.ACTIVE)
                                .build();
        }

        // ==================== CREATE BUSINESS ====================

        @Test
        @DisplayName("Should create business successfully")
        @SuppressWarnings("unchecked")
        void shouldCreateBusiness() {
                // Given
                CreateBusinessRequest request = new CreateBusinessRequest("Mi Negocio");
                BusinessResponse business = createBusinessResponse();
                when(businessService.createBusiness(userEmail, request)).thenReturn(business);

                // When
                ResponseEntity<?> response = businessController.createBusiness(request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Negocio creado exitosamente");
                assertThat(body.get("business")).isEqualTo(business);
                verify(businessService).createBusiness(userEmail, request);
        }

        // ==================== JOIN BUSINESS ====================

        @Test
        @DisplayName("Should join business successfully")
        @SuppressWarnings("unchecked")
        void shouldJoinBusiness() {
                // Given
                JoinBusinessRequest request = new JoinBusinessRequest("ABC12345");
                BusinessResponse business = createBusinessResponse();
                when(businessService.joinBusiness(userEmail, request)).thenReturn(business);

                // When
                ResponseEntity<?> response = businessController.joinBusiness(request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Te has unido al negocio exitosamente");
                assertThat(body.get("business")).isEqualTo(business);
                verify(businessService).joinBusiness(userEmail, request);
        }

        // ==================== GET BUSINESS DETAIL ====================

        @Test
        @DisplayName("Should return business detail")
        void shouldReturnBusinessDetail() {
                // Given
                BusinessDetailResponse detail = BusinessDetailResponse.builder()
                                .id(businessId)
                                .name("Mi Negocio")
                                .joinCode("ABC12345")
                                .joinCodeEnabled(true)
                                .members(List.of())
                                .categories(List.of())
                                .products(List.of())
                                .stats(null)
                                .build();
                when(businessService.getBusinessDetail(userEmail, businessId)).thenReturn(detail);

                // When
                ResponseEntity<BusinessDetailResponse> response = businessController.getBusinessDetail(
                                businessId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().name()).isEqualTo("Mi Negocio");
                verify(businessService).getBusinessDetail(userEmail, businessId);
        }

        // ==================== GET BUSINESS MEMBERS ====================

        @Test
        @DisplayName("Should return business members")
        void shouldReturnBusinessMembers() {
                // Given
                UUID memberId = UUID.randomUUID();
                BusinessMemberDetailResponse member = createMemberResponse(memberId, MembershipRole.OWNER);
                when(businessService.getBusinessMembers(userEmail, businessId)).thenReturn(List.of(member));

                // When
                ResponseEntity<List<BusinessMemberDetailResponse>> response = businessController.getBusinessMembers(
                                businessId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).role()).isEqualTo(MembershipRole.OWNER);
                verify(businessService).getBusinessMembers(userEmail, businessId);
        }

        // ==================== GET BUSINESS EMPLOYEES ====================

        @Test
        @DisplayName("Should return business employees")
        void shouldReturnBusinessEmployees() {
                // Given
                UUID employeeId = UUID.randomUUID();
                BusinessMemberDetailResponse employee = createMemberResponse(employeeId, MembershipRole.EMPLOYEE);
                when(businessService.getBusinessEmployees(userEmail, businessId)).thenReturn(List.of(employee));

                // When
                ResponseEntity<List<BusinessMemberDetailResponse>> response = businessController
                                .getBusinessEmployees(businessId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).role()).isEqualTo(MembershipRole.EMPLOYEE);
                verify(businessService).getBusinessEmployees(userEmail, businessId);
        }
}
