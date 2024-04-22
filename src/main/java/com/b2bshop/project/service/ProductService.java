package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateProductRequest;
import com.b2bshop.project.model.Product;
import com.b2bshop.project.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductService {

    @Autowired
    private EntityManager entityManager;
    private final ProductRepository productRepository;
    final SecurityService securityService;

    public ProductService(ProductRepository productRepository, SecurityService securityService) {
        this.productRepository = productRepository;
        this.securityService = securityService;
    }

    public List<Map<String, Object>> getAllProducts(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);

        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT p.id ,p.name as name, p.description as description," +
                " p.salesPrice as salesPrice, p.grossPrice as grossPrice, p.vatRate as vatRate " +
                " p.code as code, p.shop as shop, p.gtin as gtin, p.stock as stock " +
                " FROM Product p " +
                " JOIN p.shop s " +
                " WHERE 1 = 1 ";

        if (tenantId != null) {
            hqlQuery += " AND s.id = :tenantId";
        }

        Query query = session.createQuery(hqlQuery);

        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Object[]> rows = query.list();

        for (Object[] row : rows) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", row[0]);
            resultMap.put("name", row[1]);
            resultMap.put("description", row[2]);
            resultMap.put("salesPrice", row[3]);
            resultMap.put("grossPrice", row[4]);
            resultMap.put("vatRate", row[5]);
            resultMap.put("code", row[6]);
            resultMap.put("shop", row[7]);
            resultMap.put("gtin", row[8]);
            resultMap.put("stock", row[9]);
            resultList.add(resultMap);
        }
        return resultList;
    }

    public Product createProduct(CreateProductRequest request) {
        Product newProduct = Product.builder()
                .name(request.name())
                .description(request.description())
                .salesPrice(request.salesPrice())
                .grossPrice(request.grossPrice())
                .vatRate(request.vatRate())
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
            oldProduct.setVatRate(newProduct.getVatRate());
            oldProduct.setCode(newProduct.getCode());
            oldProduct.setShop(newProduct.getShop());
            oldProduct.setGtin(newProduct.getGtin());
            oldProduct.setStock(newProduct.getStock());
            productRepository.saveAndFlush(oldProduct);
            return oldProduct;
        } else return null;
    }
}
