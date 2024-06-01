package com.b2bshop.project.controller;

import com.b2bshop.project.model.Brand;
import com.b2bshop.project.service.BrandService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public List<Map<String, Object>> getAllBrands(HttpServletRequest request) {
        return brandService.getAllBrands(request);
    }

    @GetMapping("/{id}")
    public Brand getBrandById(@PathVariable Long id) {
        return brandService.findById(id);
    }

    @PostMapping()
    public ResponseEntity<Brand> createBrand(HttpServletRequest request, @RequestBody JsonNode json) {
        Brand createdBrand = brandService.createBrand(request, json);
        return ResponseEntity.ok(createdBrand);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(@PathVariable Long id, @RequestBody Brand brandDetails) {
        try {
            Brand updatedBrand = brandService.updateBrand(id, brandDetails);
            return ResponseEntity.ok(updatedBrand);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
