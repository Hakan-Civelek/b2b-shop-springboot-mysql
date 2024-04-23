package com.b2bshop.project.dto;

import com.b2bshop.project.model.Product;
import com.b2bshop.project.model.User;
import lombok.Builder;

@Builder
public record CreateBasketRequest(
        User user,
        Product product,
        Long quantity
) {
}
