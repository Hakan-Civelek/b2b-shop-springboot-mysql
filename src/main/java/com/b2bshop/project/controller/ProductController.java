package com.b2bshop.project.controller;

import com.b2bshop.project.dto.CreateProductRequest;
import com.b2bshop.project.model.Product;
import com.b2bshop.project.model.Shop;
import com.b2bshop.project.repository.ProductRepository;
import com.b2bshop.project.repository.ShopRepository;
import com.b2bshop.project.service.ProductService;
import com.b2bshop.project.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    final ProductService productService;
    final ProductRepository productRepository;
    final SecurityService securityService;
    final ShopRepository shopRepository;

    public ProductController(ProductService productService, ProductRepository productRepository, SecurityService securityService,
                             ShopRepository shopRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.securityService = securityService;
        this.shopRepository = shopRepository;
    }

    @GetMapping()
    public List<Map<String, Object>> getAllProducts(HttpServletRequest request) {
        return productService.getAllProducts( request);
    }

    @PostMapping()
    public List<Product> addProduct(@RequestBody List<CreateProductRequest> requests) {
        List<Product> createdProducts = new ArrayList<>();
        for (CreateProductRequest product : requests) {
            createdProducts.add(productService.createProduct(product));
        }
        return createdProducts;
    }

//    public Product addProduct(@RequestBody CreateProductRequest request) {
//        return productService.createProduct(request);
//    }

    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    @PutMapping("/{productId}")
    public Product updateProductById(@PathVariable Long productId, @RequestBody Product newProduct) {
        return productService.updateProductById(productId, newProduct);
    }

    @DeleteMapping("/{productId}")
    public void deleteProductById(@PathVariable Long productId) {
        productRepository.deleteById(productId);
    }

}
