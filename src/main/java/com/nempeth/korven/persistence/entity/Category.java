package com.nempeth.korven.persistence.entity;

import com.nempeth.korven.constants.CategoryType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_category_business"))
    private Business business;

    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "text")
    private CategoryType type;

    @Column(name = "display_name", columnDefinition = "text")
    private String displayName;

    @Column(name = "icon", columnDefinition = "text")
    private String icon;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Product> products;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }
}