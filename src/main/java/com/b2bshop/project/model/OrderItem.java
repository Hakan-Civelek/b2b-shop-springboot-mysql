package com.b2bshop.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name = "order_item")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long refProductId;
    private String name;
    private Double salesPrice;
    private Double grossPrice;
    private int quantity;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;
}
