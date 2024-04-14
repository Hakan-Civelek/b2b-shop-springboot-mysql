package com.b2bshop.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String name;
    private String description;
    private Double salesPrice; //vergisiz fiyat
    private Double grossPrice; //vergi dahil fiyat
    private String code;
    //category
    //brand
    //boolean isActive
    //vergiOranı -> primitive
    //images

    private String gtin;
    private int stock;
}

