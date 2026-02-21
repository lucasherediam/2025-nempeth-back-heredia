package com.nempeth.korven.service;

import com.nempeth.korven.constants.CategoryType;
import com.nempeth.korven.constants.MembershipStatus;
import com.nempeth.korven.constants.StockStatus;
import com.nempeth.korven.constants.StockUnit;
import com.nempeth.korven.persistence.entity.*;
import com.nempeth.korven.persistence.repository.BusinessMembershipRepository;
import com.nempeth.korven.persistence.repository.ProductRepository;
import com.nempeth.korven.persistence.repository.UserRepository;
import com.nempeth.korven.rest.dto.StockItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService Tests")
class StockServiceTest {

        @Mock
        private ProductRepository productRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private BusinessMembershipRepository membershipRepository;

        @InjectMocks
        private StockService stockService;

        private String userEmail;
        private UUID userId;
        private UUID businessId;
        private User testUser;
        private Business testBusiness;
        private Category testCategory;
        private BusinessMembership testMembership;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                userId = UUID.randomUUID();
                businessId = UUID.randomUUID();

                testUser = User.builder()
                                .id(userId)
                                .email(userEmail)
                                .name("Test")
                                .lastName("User")
                                .passwordHash("hashedpassword")
                                .build();

                testBusiness = Business.builder()
                                .id(businessId)
                                .name("Test Business")
                                .joinCode("TEST123")
                                .build();

                testCategory = Category.builder()
                                .id(UUID.randomUUID())
                                .business(testBusiness)
                                .name("Bebidas")
                                .type(CategoryType.CUSTOM)
                                .displayName("Bebidas")
                                .icon("🍺")
                                .build();

                testMembership = BusinessMembership.builder()
                                .user(testUser)
                                .business(testBusiness)
                                .status(MembershipStatus.ACTIVE)
                                .build();
        }

        // ==================== GET BUSINESS STOCK ====================

        @Test
        @DisplayName("Should return stock list for all products")
        void shouldReturnStockListForAllProducts() {
                // Given
                Product product1 = Product.builder()
                                .id(UUID.randomUUID())
                                .name("Cerveza")
                                .business(testBusiness)
                                .category(testCategory)
                                .stockQuantity(new BigDecimal("50"))
                                .stockUnit(StockUnit.UNIDADES)
                                .reorderPoint(new BigDecimal("10"))
                                .build();

                Product product2 = Product.builder()
                                .id(UUID.randomUUID())
                                .name("Vino")
                                .business(testBusiness)
                                .category(testCategory)
                                .stockQuantity(new BigDecimal("5"))
                                .stockUnit(StockUnit.LITROS)
                                .reorderPoint(new BigDecimal("10"))
                                .build();

                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));
                when(productRepository.findByBusinessId(businessId)).thenReturn(List.of(product1, product2));

                // When
                List<StockItemResponse> result = stockService.getBusinessStock(userEmail, businessId);

                // Then
                assertThat(result).hasSize(2);
                assertThat(result.get(0).productName()).isEqualTo("Cerveza");
                assertThat(result.get(0).stockQuantity()).isEqualByComparingTo(new BigDecimal("50"));
                assertThat(result.get(0).stockUnit()).isEqualTo(StockUnit.UNIDADES);
                assertThat(result.get(0).categoryName()).isEqualTo("Bebidas");
                assertThat(result.get(1).productName()).isEqualTo("Vino");
        }

        @Test
        @DisplayName("Should return empty list when no products exist")
        void shouldReturnEmptyListWhenNoProducts() {
                // Given
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));
                when(productRepository.findByBusinessId(businessId)).thenReturn(List.of());

                // When
                List<StockItemResponse> result = stockService.getBusinessStock(userEmail, businessId);

                // Then
                assertThat(result).isEmpty();
        }

        // ==================== STOCK STATUS CALCULATION ====================

        @Test
        @DisplayName("Should return OK status when stock is well above reorder point")
        void shouldReturnOkStatusWhenStockIsAboveReorderPoint() {
                // Given - stock 50, reorder 10, warning limit = 12.5 → OK
                Product product = Product.builder()
                                .id(UUID.randomUUID())
                                .name("Producto OK")
                                .business(testBusiness)
                                .category(testCategory)
                                .stockQuantity(new BigDecimal("50"))
                                .stockUnit(StockUnit.UNIDADES)
                                .reorderPoint(new BigDecimal("10"))
                                .build();

                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));
                when(productRepository.findByBusinessId(businessId)).thenReturn(List.of(product));

                // When
                List<StockItemResponse> result = stockService.getBusinessStock(userEmail, businessId);

                // Then
                assertThat(result.get(0).status()).isEqualTo(StockStatus.OK);
        }

        @Test
        @DisplayName("Should return BELOW_MIN status when stock is below reorder point")
        void shouldReturnBelowMinStatusWhenStockIsBelowReorderPoint() {
                // Given - stock 5, reorder 10 → BELOW_MIN
                Product product = Product.builder()
                                .id(UUID.randomUUID())
                                .name("Producto Bajo")
                                .business(testBusiness)
                                .category(testCategory)
                                .stockQuantity(new BigDecimal("5"))
                                .stockUnit(StockUnit.UNIDADES)
                                .reorderPoint(new BigDecimal("10"))
                                .build();

                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));
                when(productRepository.findByBusinessId(businessId)).thenReturn(List.of(product));

                // When
                List<StockItemResponse> result = stockService.getBusinessStock(userEmail, businessId);

                // Then
                assertThat(result.get(0).status()).isEqualTo(StockStatus.BELOW_MIN);
        }

        @Test
        @DisplayName("Should return LOW status when stock is between reorder point and warning limit")
        void shouldReturnLowStatusWhenStockIsBetweenReorderAndWarning() {
                // Given - stock 11, reorder 10, warning limit = 12.5 → LOW
                Product product = Product.builder()
                                .id(UUID.randomUUID())
                                .name("Producto Low")
                                .business(testBusiness)
                                .category(testCategory)
                                .stockQuantity(new BigDecimal("11"))
                                .stockUnit(StockUnit.UNIDADES)
                                .reorderPoint(new BigDecimal("10"))
                                .build();

                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));
                when(productRepository.findByBusinessId(businessId)).thenReturn(List.of(product));

                // When
                List<StockItemResponse> result = stockService.getBusinessStock(userEmail, businessId);

                // Then
                assertThat(result.get(0).status()).isEqualTo(StockStatus.LOW);
        }

        @Test
        @DisplayName("Should return OK status when stockQuantity is null")
        void shouldReturnOkStatusWhenStockQuantityIsNull() {
                // Given
                Product product = Product.builder()
                                .id(UUID.randomUUID())
                                .name("Producto Sin Stock")
                                .business(testBusiness)
                                .category(testCategory)
                                .stockQuantity(null)
                                .stockUnit(StockUnit.UNIDADES)
                                .reorderPoint(new BigDecimal("10"))
                                .build();

                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));
                when(productRepository.findByBusinessId(businessId)).thenReturn(List.of(product));

                // When
                List<StockItemResponse> result = stockService.getBusinessStock(userEmail, businessId);

                // Then
                assertThat(result.get(0).status()).isEqualTo(StockStatus.OK);
        }

        @Test
        @DisplayName("Should return OK status when reorderPoint is null")
        void shouldReturnOkStatusWhenReorderPointIsNull() {
                // Given
                Product product = Product.builder()
                                .id(UUID.randomUUID())
                                .name("Producto Sin Reorder")
                                .business(testBusiness)
                                .category(testCategory)
                                .stockQuantity(new BigDecimal("50"))
                                .stockUnit(StockUnit.UNIDADES)
                                .reorderPoint(null)
                                .build();

                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));
                when(productRepository.findByBusinessId(businessId)).thenReturn(List.of(product));

                // When
                List<StockItemResponse> result = stockService.getBusinessStock(userEmail, businessId);

                // Then
                assertThat(result.get(0).status()).isEqualTo(StockStatus.OK);
        }

        // ==================== VALIDATE ACCESS ====================

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
                // Given
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> stockService.getBusinessStock(userEmail, businessId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Usuario no encontrado");
        }

        @Test
        @DisplayName("Should throw exception when user has no access to business")
        void shouldThrowExceptionWhenUserHasNoAccess() {
                // Given
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> stockService.getBusinessStock(userEmail, businessId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("No tienes acceso a este negocio");
        }

        @Test
        @DisplayName("Should throw exception when membership is not active")
        void shouldThrowExceptionWhenMembershipNotActive() {
                // Given
                testMembership.setStatus(MembershipStatus.PENDING);
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));

                // When & Then
                assertThatThrownBy(() -> stockService.getBusinessStock(userEmail, businessId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Tu membresía en este negocio no está activa");
        }
}
