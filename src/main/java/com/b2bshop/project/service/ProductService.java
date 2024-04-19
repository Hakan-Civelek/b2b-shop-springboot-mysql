package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateProductRequest;
import com.b2bshop.project.model.Product;
import com.b2bshop.project.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(CreateProductRequest request) {
        Product newProduct = Product.builder()
                .name(request.name())
                .description(request.description())
                .salesPrice(request.salesPrice())
                .grossPrice(request.grossPrice())
                .code(request.code())
                .shop(request.shop())
                .gtin(request.gtin())
                .stock(request.stock())
                .build();

        return productRepository.save(newProduct);
    }

    public Product updateProductById(Long productId, Product newProduct) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            Product oldProduct = product.get();
            oldProduct.setName(newProduct.getName());
            oldProduct.setDescription(newProduct.getDescription());
            oldProduct.setSalesPrice(newProduct.getSalesPrice());
            oldProduct.setGrossPrice(newProduct.getGrossPrice());
            oldProduct.setCode(newProduct.getCode());
            oldProduct.setShop(newProduct.getShop());
            oldProduct.setGtin(newProduct.getGtin());
            oldProduct.setStock(newProduct.getStock());
            productRepository.saveAndFlush(oldProduct);
            return oldProduct;
        } else return null;
    }
}
