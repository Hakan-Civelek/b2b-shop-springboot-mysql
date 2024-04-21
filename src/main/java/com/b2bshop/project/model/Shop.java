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
    private String name;
    private String email;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "shop_address",
            joinColumns = @JoinColumn(name = "tenant_id"),
            inverseJoinColumns = @JoinColumn(name = "address_id"))
    private Set<Address> addresses;
    String phoneNumber;

    String vatNumber;
    String aboutUs;
    String privacyPolicy;
    //boolean isActive
    //favIcon (image)
    //logo (image)
}
