package com.b2bshop.project.dto;

import com.b2bshop.project.model.BasketItem;
import com.b2bshop.project.model.User;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateBasketRequest(
        User user,
        List<BasketItem> basketItems
) {
}
