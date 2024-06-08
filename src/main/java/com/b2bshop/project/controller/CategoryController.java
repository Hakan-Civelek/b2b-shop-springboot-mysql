package com.b2bshop.project.controller;

import com.b2bshop.project.model.Category;
import com.b2bshop.project.service.CategoryService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllCategories(HttpServletRequest request) {
        List<Map<String, Object>> categories = categoryService.getAllCategories(request);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<Category>> getCategoriesByShop(@PathVariable Long shopId) {
        List<Category> categories = categoryService.getCategoriesByShop(shopId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(HttpServletRequest request, @RequestBody JsonNode json) {
        Category createdCategory = categoryService.createCategory(request, json);
        return ResponseEntity.ok(createdCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategoryById(@PathVariable Long id, @RequestBody Category updatedCategory) {
        Category category = categoryService.updateCategoryById(id, updatedCategory);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
