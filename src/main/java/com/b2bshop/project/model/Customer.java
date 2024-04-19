package com.b2bshop.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "customer")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    //address
    //String vatNumber
    //phoneNumber
    //boolean isActive

    private String name;
    private String email;

    @ManyToOne
    @JoinTable(name = "customer_shop",
            joinColumns = @JoinColumn(name = "tenant_id"),
            inverseJoinColumns = @JoinColumn(name = "shop_id"))
    private Shop shop;
}
