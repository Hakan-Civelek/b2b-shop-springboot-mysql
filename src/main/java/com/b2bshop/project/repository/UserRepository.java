package com.b2bshop.project.repository;

import com.b2bshop.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String userName);
    List<User> findAllByCustomerTenantId(Long tenantId);
    List<User> findAllByShopTenantId(Long tenantId);
    List<User> findAllByCustomerShopTenantId(Long tenantId);
}