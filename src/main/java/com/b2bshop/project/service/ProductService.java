package com.b2bshop.project.service;

import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.*;
import com.b2bshop.project.repository.CustomerRepository;
import com.b2bshop.project.repository.ImageRepository;
import com.b2bshop.project.repository.ProductRepository;
import com.b2bshop.project.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductService {

    private final EntityManager entityManager;
    private final ProductRepository productRepository;
    private final SecurityService securityService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    public ProductService(ProductRepository productRepository, SecurityService securityService, JwtService jwtService, CustomerRepository customerRepository,
                          UserRepository userRepository, EntityManager entityManager, ImageRepository imageRepository) {
        this.productRepository = productRepository;
        this.securityService = securityService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.imageRepository = imageRepository;
    }

    public List<Map<String, Object>> getAllProducts(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);
        String whereCondition = " ";
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("User not found"));
        Set<Role> userRoles = user.getAuthorities();
        if (userRoles.contains(Role.ROLE_CUSTOMER_USER)) {
            whereCondition = " AND product.isActive = true ";
            tenantId = user.getCustomer().getShop().getTenantId();
        }

        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT product.id, product.name, product.description, product.salesPrice, product.grossPrice, " +
                " product.vatRate, product.code, product.shop, product.gtin, product.stock, product.isActive " +
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
            Long productId = (Long) row[0];
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
            List<Map<String, Object>> images = new ArrayList<>();
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                for (Image image : product.getImages()) {
                    Map<String, Object> imageMap = new HashMap<>();
                    imageMap.put("url", image.getUrl());
                    imageMap.put("isThumbnail", image.getIsThumbnail());
                    imageMap.put("id", image.getId());
                    images.add(imageMap);
                }
            }
            resultMap.put("images", images);

            resultList.add(resultMap);
        }
        return resultList;
    }

    @Transactional
    public Product createProduct(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("User not found"));

        Shop shop = user.getShop();

        Product product = new Product();
        product.setName(json.get("name").asText());
        product.setDescription(json.get("description").asText());
        product.setSalesPrice(json.get("salesPrice").asDouble());
        product.setGrossPrice(json.get("grossPrice").asDouble());
        product.setVatRate(json.get("vatRate").asDouble());
        product.setCode(json.get("code").asText());
        product.setGtin(json.get("gtin").asText());
        product.setStock(json.get("stock").asInt());
        product.setActive(json.get("isActive").asBoolean());
        product.setShop(shop);

        List<Image> images = new ArrayList<>();
        if (json.has("images")) {
            for (JsonNode imageNode : json.get("images")) {
                Image image = new Image();
                image.setUrl(imageNode.get("url").asText());
                image.setCreatedBy(user);
                image.setIsThumbnail(imageNode.get("isThumbnail").booleanValue());
                images.add(image);
            }
        }
        product.setImages(images);

        product = productRepository.save(product);
        imageRepository.saveAll(images);

        return product;
    }

    @Transactional
    public Product updateProductById(Long productId, Product newProduct) {
        Product product = findProductById(productId);

        product.setName(newProduct.getName());
        product.setDescription(newProduct.getDescription());
        product.setSalesPrice(newProduct.getSalesPrice());
        product.setGrossPrice(newProduct.getGrossPrice());
        product.setVatRate(newProduct.getVatRate());
        product.setCode(newProduct.getCode());
        product.setShop(newProduct.getShop());
        product.setGtin(newProduct.getGtin());
        product.setStock(newProduct.getStock());
        product.setActive(newProduct.isActive());

        List<Image> newImages = newProduct.getImages();

        if (newImages != null) {
            product.getImages().clear();

            for (Image newImage : newImages) {
                if (newImage.getId() == null) {
                    newImage = imageRepository.save(newImage);
                }
                product.getImages().add(newImage);
            }
        }

        productRepository.saveAndFlush(product);

        return product;
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

    public Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Product could not find by id: " + id));
    }
}
