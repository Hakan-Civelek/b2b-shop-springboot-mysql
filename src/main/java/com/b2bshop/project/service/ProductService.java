package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateProductRequest;
import com.b2bshop.project.model.Product;
import com.b2bshop.project.model.Role;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.CustomerRepository;
import com.b2bshop.project.repository.ProductRepository;
import com.b2bshop.project.repository.UserRepository;
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
    private final SecurityService securityService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, SecurityService securityService, JwtService jwtService, CustomerRepository customerRepository,
                          UserRepository userRepository) {
        this.productRepository = productRepository;
        this.securityService = securityService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> getAllProducts(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);
        String whereCondition = " ";
        String userName = jwtService.extractUser(token);
        User user = (userRepository.findByUsername(userName).orElseThrow(()
                -> new RuntimeException("User not found")));
        Set userRoles = user.getAuthorities();
        System.out.println("userRoles : " + userRoles);
        if (userRoles.contains(Role.ROLE_CUSTOMER_USER))
            whereCondition = " AND product.isActive = true ";

        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT product.id ,product.name as name, product.description as description," +
                " product.salesPrice as salesPrice, product.grossPrice as grossPrice, product.vatRate as vatRate, " +
                " product.code as code, product.shop as shop, product.gtin as gtin, product.stock as stock, " +
                " product.isActive as isActive " +
                " FROM Product as product " +
                " JOIN product.shop s " +
                " WHERE 1 = 1 ";

        if (tenantId != null) {
            hqlQuery += " AND s.id = :tenantId";
        }
        hqlQuery += whereCondition;

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
            resultMap.put("isActive", row[10]);
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
                .isActive(request.isActive())
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
            oldProduct.setActive(newProduct.isActive());
            productRepository.saveAndFlush(oldProduct);
            return oldProduct;
        } else return null;
    }

    public boolean checkStockById(long productId, int quantity) {
        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT product.stock " +
                " FROM Product product " +
                " WHERE product.id = :productId";

        Query query = session.createQuery(hqlQuery);
        query.setParameter("productId", productId);

        Integer stock = (Integer) query.uniqueResult(); // Assuming stock is of type Integer

        if (stock == null) {
            stock = 0; // Default value if the stock is null
        }

        return stock >= quantity;
    }
}
