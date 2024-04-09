package com.b2bshop.project.dto;

import lombok.Builder;

@Builder
public record CreateProductRequest(
        String name,
        String description,
        Double salesPrice,
        Double grossPrice,
        String sku,
        String asin,
        String gtin,
        int stock
) {
}
