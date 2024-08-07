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
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    String phoneNumber;
    @Column(unique = true)
    String vatNumber;
    String aboutUs;
    String privacyPolicy;

    //address
    //favIcon (image)
    //logo (image)
}
