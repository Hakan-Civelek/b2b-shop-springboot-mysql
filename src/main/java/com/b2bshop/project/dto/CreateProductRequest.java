package com.b2bshop.project.dto;

import com.b2bshop.project.model.Category;
import com.b2bshop.project.model.Shop;
import lombok.Builder;

@Builder
public record CreateProductRequest(
        String name,
        String description,
        Double salesPrice,
        Double grossPrice,
        Double vatRate,
        String code,
        Shop shop,
        Category category,
        String gtin,
        int stock,
        Boolean isActive
) {
}
