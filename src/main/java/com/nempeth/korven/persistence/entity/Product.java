package com.nempeth.korven.persistence.entity;

import com.nempeth.korven.constants.StockUnit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false, foreignKey = @ForeignKey(name = "fk_products_business"))
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_products_category"))
    private Category category;

    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(name = "stock_quantity", precision = 12, scale = 2)
    private BigDecimal stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_unit", length = 50)
    private StockUnit stockUnit;

    @Column(name = "reorder_point", precision = 12, scale = 2)
    private BigDecimal reorderPoint;

    @OneToMany(mappedBy = "product", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    private Set<SaleItem> saleItems;

    @PrePersist
    public void prePersist() {
        if (id == null)
            id = UUID.randomUUID();
    }
}
