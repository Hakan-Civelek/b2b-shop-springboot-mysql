package com.b2bshop.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    //address
    //String vatNumber
    //String aboutUs
    //String privacyPolicy
    //phoneNumber
    //boolean isActive
    //favIcon (image)
    //logo (image)
}
