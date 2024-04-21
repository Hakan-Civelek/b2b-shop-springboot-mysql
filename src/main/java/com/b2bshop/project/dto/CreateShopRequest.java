package com.b2bshop.project.dto;

import com.b2bshop.project.model.Address;
import lombok.Builder;

import java.util.Set;

@Builder
public record CreateShopRequest(
        String name,
        String email,
        Set<Address> addresses,
        String phoneNumber,
        String vatNumber,
        String aboutUs,
        String privacyPolicy
) {
}
