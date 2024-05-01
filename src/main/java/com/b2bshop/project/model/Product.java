package com.b2bshop.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@Table(name = "product")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private String description;
    private Double salesPrice; //vergisiz fiyat
    private Double grossPrice; //vergi dahil fiyat
    private Double vatRate;
    @Column(unique = true)
    private String code;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinTable(name = "product_shop",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tenant_id"))
    private Shop shop;

    //category
    //brand
    //images

    @Column(unique = true)
    private String gtin;
    private int stock;
    private boolean isActive = false;
}

