package com.b2bshop.project.dto;

import com.b2bshop.project.model.Shop;
import lombok.Builder;

@Builder
public record CreateProductRequest(
        String name,
        String description,
        Double salesPrice,
        Double grossPrice,
        String code,
        Shop shop,
        String gtin,
        int stock
) {
}
