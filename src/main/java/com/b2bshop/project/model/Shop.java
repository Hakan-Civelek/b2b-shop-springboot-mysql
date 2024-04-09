package com.b2bshop.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Entity
@Table(name = "shop")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @OneToMany
    @JoinTable(name = "company_shop",
            joinColumns = @JoinColumn(name = "tenant_id"),
            inverseJoinColumns = @JoinColumn(name = "shop_id"))
    private Set<Company> companies;

    private String name;
    private String email;

    @OneToMany
    @JoinTable(name = "shop_user",
            joinColumns = @JoinColumn(name = "tenant_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users;

    @OneToMany
    @JoinTable(name = "shop_product",
            joinColumns = @JoinColumn(name = "tenantId"),
            inverseJoinColumns = @JoinColumn(name = "productId"))
    private Set<Product> products;
}
