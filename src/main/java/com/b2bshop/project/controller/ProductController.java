package com.b2bshop.project.controller;

import com.b2bshop.project.model.Product;
import com.b2bshop.project.repository.ProductRepository;
import com.b2bshop.project.service.ProductService;
import com.b2bshop.project.service.SecurityService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    final ProductService productService;
    final ProductRepository productRepository;
    final SecurityService securityService;

    public ProductController(ProductService productService, ProductRepository productRepository, SecurityService securityService) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.securityService = securityService;
    }

    @GetMapping()
    public List<Map<String, Object>> getAllProducts(HttpServletRequest request,
                                                    @RequestParam(required = false) Long categoryId,
                                                    @RequestParam(required = false) List<Long> brandIds) {
        return productService.getAllProducts(request, categoryId, brandIds);
    }

//    @PostMapping()
//    public List<Product> addProduct(@RequestBody List<CreateProductRequest> requests) {
//        List<Product> createdProducts = new ArrayList<>();
//        for (CreateProductRequest product : requests) {
//            createdProducts.add(productService.createProduct(product));
//        }
//        return createdProducts;
//    }

    @PostMapping()
    public Product addProduct(HttpServletRequest request, @RequestBody JsonNode json) {
        return productService.createProduct(request, json);
    }

    @GetMapping("/{productId}")
    public Map<String, Object> findProductMapById(@PathVariable Long productId) {
        return productService.findProductById(productId);
    }

    @PutMapping("/{productId}")
    public Product updateProductById(@PathVariable Long productId, @RequestBody JsonNode json) {
        return productService.updateProductById(productId, json);
    }

    @DeleteMapping("/{productId}")
    public void deleteProductById(@PathVariable Long productId) {
        productRepository.deleteById(productId);
    }

}
