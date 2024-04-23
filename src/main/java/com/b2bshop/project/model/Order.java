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
    //    orderNumber //(Müşteriye özelleştirirsen güzel olur)
    private String orderNote;
    //    address
    @OneToMany
    private List<Product> products;
    private Date orderDate;
    //    orderStatus ? sipariş verildi, onaylandı, tamamlandı
    @OneToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;
    //    invoiceAddress
//    receiverAddress
    private Double totalPrice;
    private Double withoutTaxPrice;
    private Double totalTax;
}
