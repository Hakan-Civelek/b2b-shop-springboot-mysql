package com.b2bshop.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(unique = true)
    String orderNumber;
    private String orderNote;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
    private Date orderDate;
//    orderStatus ? sipariş verildi, onaylandı, tamamlandı
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;
    @ManyToOne
    private Address invoiceAddress;
    @ManyToOne
    private Address receiverAddress;
    private Double totalPrice;
    private Double withoutTaxPrice;
    private Double totalTax;
}
