package com.b2bshop.project.repository;

import com.b2bshop.project.model.Basket;
import com.b2bshop.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BasketRepository extends JpaRepository<Basket, Long> {
    Optional<Basket> findByUserId(Long id);
}
