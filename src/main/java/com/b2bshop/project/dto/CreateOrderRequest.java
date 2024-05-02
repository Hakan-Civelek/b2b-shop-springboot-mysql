package com.b2bshop.project.dto;

import com.b2bshop.project.model.Customer;
import com.b2bshop.project.model.OrderStatus;
import com.b2bshop.project.model.Product;
import com.b2bshop.project.model.User;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Builder
public record CreateOrderRequest(
        Customer customer,
        String orderNote,
        List<Product> products,
        Date orderDate,
        User createdBy,
        BigDecimal totalPrice,
        BigDecimal withoutTaxPrice,
        BigDecimal totalTax,
        OrderStatus orderStatus
) {
}
