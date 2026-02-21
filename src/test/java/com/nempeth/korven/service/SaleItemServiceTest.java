package com.nempeth.korven.service;

import com.nempeth.korven.constants.CategoryType;
import com.nempeth.korven.constants.MembershipStatus;
import com.nempeth.korven.persistence.entity.*;
import com.nempeth.korven.persistence.repository.*;
import com.nempeth.korven.rest.dto.CreateSaleItemRequest;
import com.nempeth.korven.rest.dto.SaleItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleItemService Tests")
class SaleItemServiceTest {

        @Mock
        private SaleItemRepository saleItemRepository;

        @Mock
        private SaleRepository saleRepository;

        @Mock
        private ProductRepository productRepository;

        @Mock
        private BusinessMembershipRepository membershipRepository;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private SaleItemService saleItemService;

        private String userEmail;
        private UUID userId;
        private UUID businessId;
        private UUID saleId;
        private UUID productId;
        private User testUser;
        private Business testBusiness;
        private Category testCategory;
        private Product testProduct;
        private Sale testSale;
        private BusinessMembership testMembership;

        @BeforeEach
        void setUp() {
                userEmail = "test@example.com";
                userId = UUID.randomUUID();
                businessId = UUID.randomUUID();
                saleId = UUID.randomUUID();
                productId = UUID.randomUUID();

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

                testProduct = Product.builder()
                                .id(productId)
                                .business(testBusiness)
                                .category(testCategory)
                                .name("Cerveza")
                                .price(new BigDecimal("500.00"))
                                .cost(new BigDecimal("250.00"))
                                .build();

                testSale = Sale.builder()
                                .id(saleId)
                                .business(testBusiness)
                                .createdByUser(testUser)
                                .createdByUserName("Test User")
                                .totalAmount(BigDecimal.ZERO)
                                .build();

                testMembership = BusinessMembership.builder()
                                .user(testUser)
                                .business(testBusiness)
                                .status(MembershipStatus.ACTIVE)
                                .build();
        }

        private void setupValidAccess() {
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));
        }

        // ==================== ADD ITEM TO SALE ====================

        @Test
        @DisplayName("Should add new item to sale successfully")
        void shouldAddNewItemToSaleSuccessfully() {
                // Given
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 3);
                UUID itemId = UUID.randomUUID();

                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
                when(productRepository.findByIdAndBusinessId(productId, businessId))
                                .thenReturn(Optional.of(testProduct));
                when(saleItemRepository.findBySaleIdAndProductId(saleId, productId))
                                .thenReturn(Optional.empty());

                SaleItem savedItem = SaleItem.builder()
                                .id(itemId)
                                .sale(testSale)
                                .product(testProduct)
                                .productNameAtSale("Cerveza")
                                .categoryName("Bebidas")
                                .quantity(3)
                                .unitPrice(new BigDecimal("500.00"))
                                .unitCost(new BigDecimal("250.00"))
                                .lineTotal(new BigDecimal("1500.00"))
                                .build();
                when(saleItemRepository.save(any(SaleItem.class))).thenReturn(savedItem);

                // When
                UUID result = saleItemService.addItemToSale(userEmail, businessId, saleId, request);

                // Then
                assertThat(result).isEqualTo(itemId);

                ArgumentCaptor<SaleItem> captor = ArgumentCaptor.forClass(SaleItem.class);
                verify(saleItemRepository).save(captor.capture());
                SaleItem captured = captor.getValue();
                assertThat(captured.getProductNameAtSale()).isEqualTo("Cerveza");
                assertThat(captured.getCategoryName()).isEqualTo("Bebidas");
                assertThat(captured.getQuantity()).isEqualTo(3);
                assertThat(captured.getUnitPrice()).isEqualByComparingTo(new BigDecimal("500.00"));
                assertThat(captured.getUnitCost()).isEqualByComparingTo(new BigDecimal("250.00"));
                assertThat(captured.getLineTotal()).isEqualByComparingTo(new BigDecimal("1500.00"));
        }

        @Test
        @DisplayName("Should update existing item when product already in sale")
        void shouldUpdateExistingItemWhenProductAlreadyInSale() {
                // Given
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 5);
                UUID existingItemId = UUID.randomUUID();

                SaleItem existingItem = SaleItem.builder()
                                .id(existingItemId)
                                .sale(testSale)
                                .product(testProduct)
                                .productNameAtSale("Cerveza")
                                .categoryName("Bebidas")
                                .quantity(2)
                                .unitPrice(new BigDecimal("500.00"))
                                .unitCost(new BigDecimal("250.00"))
                                .lineTotal(new BigDecimal("1000.00"))
                                .build();

                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
                when(productRepository.findByIdAndBusinessId(productId, businessId))
                                .thenReturn(Optional.of(testProduct));
                when(saleItemRepository.findBySaleIdAndProductId(saleId, productId))
                                .thenReturn(Optional.of(existingItem));
                when(saleItemRepository.save(any(SaleItem.class))).thenReturn(existingItem);

                // When
                UUID result = saleItemService.addItemToSale(userEmail, businessId, saleId, request);

                // Then
                assertThat(result).isEqualTo(existingItemId);
                assertThat(existingItem.getQuantity()).isEqualTo(5);
                assertThat(existingItem.getLineTotal()).isEqualByComparingTo(new BigDecimal("2500.00"));
                verify(saleItemRepository).save(existingItem);
        }

        @Test
        @DisplayName("Should delete item when quantity is 0 and item exists")
        void shouldDeleteItemWhenQuantityIsZeroAndItemExists() {
                // Given
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 0);
                UUID existingItemId = UUID.randomUUID();

                SaleItem existingItem = SaleItem.builder()
                                .id(existingItemId)
                                .sale(testSale)
                                .product(testProduct)
                                .quantity(2)
                                .lineTotal(new BigDecimal("1000.00"))
                                .build();

                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
                when(productRepository.findByIdAndBusinessId(productId, businessId))
                                .thenReturn(Optional.of(testProduct));
                when(saleItemRepository.findBySaleIdAndProductId(saleId, productId))
                                .thenReturn(Optional.of(existingItem));

                // When
                UUID result = saleItemService.addItemToSale(userEmail, businessId, saleId, request);

                // Then
                assertThat(result).isEqualTo(existingItemId);
                verify(saleItemRepository).delete(existingItem);
                verify(saleItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when quantity is 0 and item does not exist")
        void shouldThrowExceptionWhenQuantityZeroAndItemNotExists() {
                // Given
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 0);

                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
                when(productRepository.findByIdAndBusinessId(productId, businessId))
                                .thenReturn(Optional.of(testProduct));
                when(saleItemRepository.findBySaleIdAndProductId(saleId, productId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> saleItemService.addItemToSale(userEmail, businessId, saleId, request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("No se puede establecer cantidad 0");
        }

        @Test
        @DisplayName("Should throw exception when sale not found on addItem")
        void shouldThrowExceptionWhenSaleNotFoundOnAddItem() {
                // Given
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 2);

                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> saleItemService.addItemToSale(userEmail, businessId, saleId, request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Venta no encontrada");
        }

        @Test
        @DisplayName("Should throw exception when sale does not belong to business on addItem")
        void shouldThrowExceptionWhenSaleNotInBusinessOnAddItem() {
                // Given
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 2);

                Business otherBusiness = Business.builder()
                                .id(UUID.randomUUID())
                                .name("Other Business")
                                .build();
                Sale otherSale = Sale.builder()
                                .id(saleId)
                                .business(otherBusiness)
                                .build();

                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.of(otherSale));

                // When & Then
                assertThatThrownBy(() -> saleItemService.addItemToSale(userEmail, businessId, saleId, request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("La venta no pertenece a este negocio");
        }

        @Test
        @DisplayName("Should throw exception when product not found on addItem")
        void shouldThrowExceptionWhenProductNotFoundOnAddItem() {
                // Given
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 2);

                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
                when(productRepository.findByIdAndBusinessId(productId, businessId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> saleItemService.addItemToSale(userEmail, businessId, saleId, request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Producto no encontrado en este negocio");
        }

        // ==================== GET SALE ITEMS ====================

        @Test
        @DisplayName("Should return sale items successfully")
        void shouldReturnSaleItemsSuccessfully() {
                // Given
                UUID itemId = UUID.randomUUID();
                SaleItem item = SaleItem.builder()
                                .id(itemId)
                                .sale(testSale)
                                .productNameAtSale("Cerveza")
                                .categoryName("Bebidas")
                                .quantity(3)
                                .unitPrice(new BigDecimal("500.00"))
                                .unitCost(new BigDecimal("250.00"))
                                .lineTotal(new BigDecimal("1500.00"))
                                .build();

                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
                when(saleItemRepository.findBySaleId(saleId)).thenReturn(List.of(item));

                // When
                List<SaleItemResponse> result = saleItemService.getSaleItems(userEmail, businessId, saleId);

                // Then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).id()).isEqualTo(itemId);
                assertThat(result.get(0).productName()).isEqualTo("Cerveza");
                assertThat(result.get(0).categoryName()).isEqualTo("Bebidas");
                assertThat(result.get(0).quantity()).isEqualTo(3);
                assertThat(result.get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("500.00"));
                assertThat(result.get(0).lineTotal()).isEqualByComparingTo(new BigDecimal("1500.00"));
        }

        @Test
        @DisplayName("Should return empty list when sale has no items")
        void shouldReturnEmptyListWhenSaleHasNoItems() {
                // Given
                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
                when(saleItemRepository.findBySaleId(saleId)).thenReturn(List.of());

                // When
                List<SaleItemResponse> result = saleItemService.getSaleItems(userEmail, businessId, saleId);

                // Then
                assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when sale not found on getSaleItems")
        void shouldThrowExceptionWhenSaleNotFoundOnGetSaleItems() {
                // Given
                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> saleItemService.getSaleItems(userEmail, businessId, saleId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Venta no encontrada");
        }

        @Test
        @DisplayName("Should throw exception when sale not in business on getSaleItems")
        void shouldThrowExceptionWhenSaleNotInBusinessOnGetSaleItems() {
                // Given
                Business otherBusiness = Business.builder()
                                .id(UUID.randomUUID())
                                .name("Other Business")
                                .build();
                Sale otherSale = Sale.builder()
                                .id(saleId)
                                .business(otherBusiness)
                                .build();

                setupValidAccess();
                when(saleRepository.findById(saleId)).thenReturn(Optional.of(otherSale));

                // When & Then
                assertThatThrownBy(() -> saleItemService.getSaleItems(userEmail, businessId, saleId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("La venta no pertenece a este negocio");
        }

        // ==================== VALIDATE ACCESS ====================

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
                // Given
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 2);
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> saleItemService.addItemToSale(userEmail, businessId, saleId, request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Usuario no encontrado");
        }

        @Test
        @DisplayName("Should throw exception when user has no access to business")
        void shouldThrowExceptionWhenUserHasNoAccess() {
                // Given
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 2);
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> saleItemService.addItemToSale(userEmail, businessId, saleId, request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("No tienes acceso a este negocio");
        }

        @Test
        @DisplayName("Should throw exception when membership is not active")
        void shouldThrowExceptionWhenMembershipNotActive() {
                // Given
                testMembership.setStatus(MembershipStatus.PENDING);
                CreateSaleItemRequest request = new CreateSaleItemRequest(productId, 2);
                when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(testUser));
                when(membershipRepository.findByBusinessIdAndUserId(businessId, userId))
                                .thenReturn(Optional.of(testMembership));

                // When & Then
                assertThatThrownBy(() -> saleItemService.addItemToSale(userEmail, businessId, saleId, request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Tu membresía en este negocio no está activa");
        }
}
