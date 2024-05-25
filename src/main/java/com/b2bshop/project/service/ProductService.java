package com.b2bshop.project.service;

import com.b2bshop.project.exception.CustomerNotFoundException;
import com.b2bshop.project.exception.ProductNotFoundException;
import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.*;
import com.b2bshop.project.repository.*;
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
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, SecurityService securityService, JwtService jwtService, CustomerRepository customerRepository,
                          UserRepository userRepository, EntityManager entityManager, ImageRepository imageRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.securityService = securityService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.imageRepository = imageRepository;
        this.categoryRepository = categoryRepository;
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
                " product.vatRate, product.code, product.shop, product.gtin, product.stock, product.isActive, category " +
                " FROM Product as product " +
                " JOIN product.shop s " +
                " LEFT JOIN product.category category " +
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
            resultMap.put("category", getCategoryMap((Category) row[11]));

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

    private Map<String, Object> getCategoryMap(Category category) {
        if (category == null) {
            return null;
        }
        Map<String, Object> categoryMap = new HashMap<>();
        categoryMap.put("id", category.getId());
        categoryMap.put("name", category.getName());
        return categoryMap;
    }


    @Transactional
    public Product createProduct(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));
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

        JsonNode categoryNode = json.get("category");
        Long categoryId = categoryNode.get("id").asLong();

        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException("Category could not find by id: " + categoryId));

        product.setCategory(category);

        List<Image> images = new ArrayList<>();
        if (json.has("images")) {
            for (JsonNode imageNode : json.get("images")) {
                Image image = new Image();
                image.setUrl(imageNode.get("url").asText());
                image.setCreatedBy(user);
                image.setIsThumbnail(imageNode.get("isThumbnail").asBoolean());
                images.add(image);
            }
        }
        product.setImages(images);

        product = productRepository.save(product);
        imageRepository.saveAll(images);

        return product;
    }

    @Transactional
    public Product updateProductById(HttpServletRequest request, Long productId, JsonNode json) {
        Product product = findProductById(productId);

        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("User not found"));

        product.setName(json.get("name").asText());
        product.setDescription(json.get("description").asText());
        product.setSalesPrice(json.get("salesPrice").asDouble());
        product.setGrossPrice(json.get("grossPrice").asDouble());
        product.setVatRate(json.get("vatRate").asDouble());
        product.setCode(json.get("code").asText());
        product.setGtin(json.get("gtin").asText());
        product.setStock(json.get("stock").asInt());
        product.setActive(json.get("isActive").asBoolean());

        JsonNode categoryNode = json.get("category");
        Long categoryId = categoryNode.get("id").asLong();

        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException("Category could not find by id: " + categoryId));

        product.setCategory(category);

        List<Image> existingImages = product.getImages();
        List<Image> newImages = new ArrayList<>();
        if (json.has("images")) {
            for (JsonNode imageNode : json.get("images")) {
                Image image = new Image();
                image.setUrl(imageNode.get("url").asText());
                image.setCreatedBy(user);
                image.setIsThumbnail(imageNode.get("isThumbnail").asBoolean());
                newImages.add(image);
            }
        }

        for (Image existingImage : existingImages) {
            Optional<Image> matchedImage = newImages.stream()
                    .filter(newImage -> newImage.getUrl().equals(existingImage.getUrl()))
                    .findFirst();

            matchedImage.ifPresent(newImage -> {
                existingImage.setIsThumbnail(newImage.getIsThumbnail());
                newImages.remove(newImage);
            });
        }

        for (Image imageToRemove : newImages) {
            existingImages.removeIf(existingImage -> existingImage.getUrl().equals(imageToRemove.getUrl()));
            imageRepository.delete(imageToRemove);
        }

        for (Image newImage : newImages) {
            existingImages.add(newImage);
        }

        product = productRepository.saveAndFlush(product);
        imageRepository.saveAll(existingImages);

        return product;
    }

    public boolean checkStockById(long productId, int quantity) {
        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT product.stock " +
                " FROM Product product " +
                " WHERE product.id = :productId";

        Query query = session.createQuery(hqlQuery);
        query.setParameter("productId", productId);

        Integer stock = (Integer) query.uniqueResult();

        if (stock == null) {
            stock = 0;
        }

        return stock >= quantity;
    }

    public Map<String, Object> getProductById(HttpServletRequest request, Long productId) {
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
                " product.vatRate, product.code, product.shop, product.gtin, product.stock, product.isActive, category " +
                " FROM Product as product " +
                " JOIN product.shop s " +
                " LEFT JOIN product.category category " +
                " WHERE product.id = :productId ";

        if (tenantId != null) {
            hqlQuery += " AND s.id = :tenantId";
        }
        hqlQuery += whereCondition;

        Query query = session.createQuery(hqlQuery);
        query.setParameter("productId", productId);

        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }

        Object[] row = (Object[]) query.uniqueResult();
        if (row == null) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }

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
        resultMap.put("category", getCategoryMap((Category) row[11]));

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

        return resultMap;
    }
    public Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(()
                -> new ProductNotFoundException("Product could not find by id: " + id));
    }
}
