package com.b2bshop.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @ElementCollection
    private Set<Long> childCategoryIds = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Product> products;
}
