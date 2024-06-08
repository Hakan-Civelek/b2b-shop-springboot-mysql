package com.b2bshop.project.repository;

import com.b2bshop.project.model.Category;
import com.b2bshop.project.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByShop(Shop shop);
}
