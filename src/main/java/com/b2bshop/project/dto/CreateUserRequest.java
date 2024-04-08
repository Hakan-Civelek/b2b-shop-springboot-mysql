package com.b2bshop.project.dto;

import com.b2bshop.project.model.Role;
import lombok.Builder;
import java.util.Set;


@Builder
public record CreateUserRequest(
        String name,
        String username,
        String password,
        String email,
        Set<Role> authorities
){
}