package com.b2bshop.project.repository;

import com.b2bshop.project.model.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasketItemRepository  extends JpaRepository<BasketItem, Long> {
}
