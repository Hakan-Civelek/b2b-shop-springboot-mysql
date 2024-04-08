package com.b2bshop.project.dto;

import com.b2bshop.project.model.Company;
import com.b2bshop.project.model.User;
import lombok.Builder;

import java.util.Set;

@Builder
public record CreateShopRequest(
        String name,
        String email,
        Set<Company> companies,
        Set<User>users
) {
}
