package com.nempeth.korven.rest;

import com.nempeth.korven.constants.CategoryType;
import com.nempeth.korven.rest.dto.CategoryResponse;
import com.nempeth.korven.rest.dto.CreateCategoryRequest;
import com.nempeth.korven.rest.dto.UpdateCategoryRequest;
import com.nempeth.korven.service.CategoryService;
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
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

        @Mock
        private CategoryService categoryService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private CategoryController categoryController;

        private String userEmail;
        private UUID businessId;
        private UUID categoryId;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                businessId = UUID.randomUUID();
                categoryId = UUID.randomUUID();
                when(authentication.getName()).thenReturn(userEmail);
        }

        private CategoryResponse createCategoryResponse() {
                return CategoryResponse.builder()
                                .id(categoryId)
                                .name("Bebidas")
                                .type(CategoryType.CUSTOM)
                                .displayName("Bebidas")
                                .icon("🥤")
                                .build();
        }

        // ==================== GET ALL CATEGORIES ====================

        @Test
        @DisplayName("Should return all categories for a business")
        void shouldReturnAllCategories() {
                // Given
                CategoryResponse category = createCategoryResponse();
                when(categoryService.getCategoriesByBusiness(userEmail, businessId))
                                .thenReturn(List.of(category));

                // When
                ResponseEntity<List<CategoryResponse>> response = categoryController.getAllCategories(
                                businessId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody().get(0).name()).isEqualTo("Bebidas");
                verify(categoryService).getCategoriesByBusiness(userEmail, businessId);
        }

        // ==================== GET CUSTOM CATEGORIES ====================

        @Test
        @DisplayName("Should return custom categories for a business")
        void shouldReturnCustomCategories() {
                // Given
                CategoryResponse category = createCategoryResponse();
                when(categoryService.getCustomCategoriesByBusiness(userEmail, businessId))
                                .thenReturn(List.of(category));

                // When
                ResponseEntity<List<CategoryResponse>> response = categoryController.getCustomCategories(
                                businessId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).hasSize(1);
                verify(categoryService).getCustomCategoriesByBusiness(userEmail, businessId);
        }

        // ==================== CREATE CUSTOM CATEGORY ====================

        @Test
        @DisplayName("Should create custom category and return it")
        @SuppressWarnings("unchecked")
        void shouldCreateCustomCategory() {
                // Given
                CreateCategoryRequest request = new CreateCategoryRequest("Postres", "Postres", "🍰");
                CategoryResponse created = CategoryResponse.builder()
                                .id(UUID.randomUUID())
                                .name("Postres")
                                .type(CategoryType.CUSTOM)
                                .displayName("Postres")
                                .icon("🍰")
                                .build();
                when(categoryService.createCustomCategory(userEmail, businessId, request)).thenReturn(created);

                // When
                ResponseEntity<?> response = categoryController.createCustomCategory(
                                businessId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Categoría creada exitosamente");
                assertThat(body.get("category")).isEqualTo(created);
                verify(categoryService).createCustomCategory(userEmail, businessId, request);
        }

        // ==================== UPDATE CUSTOM CATEGORY ====================

        @Test
        @DisplayName("Should update custom category and return it")
        @SuppressWarnings("unchecked")
        void shouldUpdateCustomCategory() {
                // Given
                UpdateCategoryRequest request = new UpdateCategoryRequest("Postres Fríos", "Postres Fríos", "🍨");
                CategoryResponse updated = CategoryResponse.builder()
                                .id(categoryId)
                                .name("Postres Fríos")
                                .type(CategoryType.CUSTOM)
                                .displayName("Postres Fríos")
                                .icon("🍨")
                                .build();
                when(categoryService.updateCustomCategory(userEmail, businessId, categoryId, request))
                                .thenReturn(updated);

                // When
                ResponseEntity<?> response = categoryController.updateCustomCategory(
                                businessId, categoryId, request, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Categoría actualizada exitosamente");
                assertThat(body.get("category")).isEqualTo(updated);
                verify(categoryService).updateCustomCategory(userEmail, businessId, categoryId, request);
        }

        // ==================== DELETE CUSTOM CATEGORY ====================

        @Test
        @DisplayName("Should delete custom category successfully")
        @SuppressWarnings("unchecked")
        void shouldDeleteCustomCategory() {
                // Given
                doNothing().when(categoryService).deleteCustomCategory(userEmail, businessId, categoryId);

                // When
                ResponseEntity<?> response = categoryController.deleteCustomCategory(
                                businessId, categoryId, authentication);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                assertThat(body.get("message")).isEqualTo("Categoría eliminada exitosamente");
                verify(categoryService).deleteCustomCategory(userEmail, businessId, categoryId);
        }
}
