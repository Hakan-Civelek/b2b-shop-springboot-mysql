package com.b2bshop.project.repository;

import com.b2bshop.project.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

}