package com.b2bshop.project.dto;

import com.b2bshop.project.model.User;
import lombok.Builder;
import java.util.Set;

@Builder
public record CreateCompanyRequest(
        String name,
        String email,
        Set<User> users
) {
}