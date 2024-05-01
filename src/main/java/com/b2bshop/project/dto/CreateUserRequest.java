package com.b2bshop.project.dto;

import com.b2bshop.project.model.Customer;
import com.b2bshop.project.model.Role;
import com.b2bshop.project.model.Shop;
import lombok.Builder;

import java.util.Set;

@Builder
public record CreateUserRequest(
        String name,
        String username,
        String password,
        String email,
        String phoneNumber,
        Set<Role> authorities,
        Shop shop,
        Customer customer,
        Boolean isActive
) {
}