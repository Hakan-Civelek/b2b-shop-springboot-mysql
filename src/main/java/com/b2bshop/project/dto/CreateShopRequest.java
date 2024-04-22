package com.b2bshop.project.dto;

import lombok.Builder;

@Builder
public record CreateShopRequest(
        String name,
        String email,
        String phoneNumber,
        String vatNumber,
        String aboutUs,
        String privacyPolicy
) {
}
