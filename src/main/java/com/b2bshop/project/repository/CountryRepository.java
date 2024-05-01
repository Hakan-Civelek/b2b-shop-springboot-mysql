package com.b2bshop.project.repository;

import com.b2bshop.project.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
}
