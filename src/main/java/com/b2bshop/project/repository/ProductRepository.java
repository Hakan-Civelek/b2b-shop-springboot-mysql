package com.b2bshop.project.repository;

import com.b2bshop.project.model.Product;
import com.b2bshop.project.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface ProductRepository extends JpaRepository<Product, Long> {
    ArrayList<Product> findByShop(Shop shop);
}