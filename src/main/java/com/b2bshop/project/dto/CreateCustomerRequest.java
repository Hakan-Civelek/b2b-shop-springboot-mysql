package com.b2bshop.project.dto;

import com.b2bshop.project.model.Address;
import com.b2bshop.project.model.Shop;
import lombok.Builder;

import java.util.Set;

@Builder
public record CreateCustomerRequest(
        String name,
        String email,
        Shop shop,
        Set<Address> addresses,
        String vatNumber,
        String phoneNumber
) {
}