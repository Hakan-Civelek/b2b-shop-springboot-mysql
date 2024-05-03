//package com.b2bshop.project.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.Set;
//
//@Data
//@Entity
//@Table(name = "category")
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Category {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", nullable = false)
//    private Long id;
//
//    @Column(unique = true)
//    private String name;
//    private Long parentId;
//
//    @OneToMany
//    private Set<Category> subCategories;
//}
