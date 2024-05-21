package com.b2bshop.project.repository;

import com.b2bshop.project.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}