package com.b2bshop.project.dto;

import com.b2bshop.project.model.Shop;
import lombok.Builder;

@Builder
public record CreateCustomerRequest(
        String name,
        String email,
        Shop shop,
        String vatNumber,
        String phoneNumber
) {
}