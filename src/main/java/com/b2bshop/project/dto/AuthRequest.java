package com.b2bshop.project.dto;

public record AuthRequest(
        Long tenantId,
        String username,
        String password
) {
}