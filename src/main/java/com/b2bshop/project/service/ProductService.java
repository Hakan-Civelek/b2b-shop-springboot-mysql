package com.b2bshop.project.service;

import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.*;
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
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final EntityManager entityManager;
    private final ProductRepository productRepository;
    private final SecurityService securityService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final BrandService brandService;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, SecurityService securityService, JwtService jwtService,
                          UserRepository userRepository, EntityManager entityManager, ImageRepository imageRepository,
                          BrandService brandService, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.securityService = securityService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.imageRepository = imageRepository;
        this.brandService = brandService;
        this.categoryService = categoryService;
    }

    public List<Map<String, Object>> getAllProducts(HttpServletRequest request, Long categoryId, List<Long> brandIds) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);
        String whereCondition = " ";
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("User not found"));
        Set<Role> userRoles = user.getAuthorities();
        if (userRoles.contains(Role.ROLE_CUSTOMER_USER)) {
            whereCondition = " AND product.isActive = true AND product.stock > 0 ";
            tenantId = user.getCustomer().getShop().getTenantId();
        }

        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT product.id, product.name, product.description, product.salesPrice, product.grossPrice, " +
                " product.vatRate, product.code, product.gtin, product.stock, product.isActive, " +
                " brand.id as brandId, brand.name as brandName, image.id as imageId, image.url as imageUrl, " +
                " image.isThumbnail as imageIsThumbnail, " +
                " category.id as categoryId, category.name as categoryName, " +
                " parentCategory.id as parentCategoryId, parentCategory.name as parentCategoryName " +
                " FROM Product as product " +
                " JOIN product.shop as shop " +
                " LEFT JOIN product.brand as brand " +
                " LEFT JOIN product.images as image " +
                " LEFT JOIN product.category as category " +
                " LEFT JOIN category.parentCategory as parentCategory " +
                " WHERE 1 = 1 ";

        if (tenantId != null) {
            hqlQuery += " AND shop.id = :tenantId";
        }

        if (brandIds != null && !brandIds.isEmpty()) {
            hqlQuery += " AND brand.id IN :brandIds";
        }

        Set<Long> categoryIds = null;
        if (categoryId != null) {
            Category category = categoryService.findById(categoryId);
            if (category != null) {
                categoryIds = new HashSet<>(category.getChildCategoryIds());
                categoryIds.add(categoryId);
            }
            hqlQuery += " AND category.id IN :categoryIds";
        }

        hqlQuery += whereCondition;

        Query query = session.createQuery(hqlQuery);
        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }
        if (brandIds != null && !brandIds.isEmpty()) {
            query.setParameter("brandIds", brandIds);
        }
        if (categoryId != null && categoryIds != null) {
            query.setParameter("categoryIds", categoryIds);
        }

        List<Object[]> rows = query.list();
        Map<Long, Map<String, Object>> productsMap = new HashMap<>();

        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            Map<String, Object> productMap = productsMap.get(productId);

            if (productMap == null) {
                productMap = new HashMap<>();
                productMap.put("id", row[0]);
                productMap.put("name", row[1]);
                productMap.put("description", row[2]);
                productMap.put("salesPrice", row[3]);
                productMap.put("grossPrice", row[4]);
                productMap.put("vatRate", row[5]);
                productMap.put("code", row[6]);
                productMap.put("gtin", row[7]);
                productMap.put("stock", row[8]);
                productMap.put("active", row[9]);

                Map<String, Object> brandMap = new HashMap<>();
                if (row[10] != null) {
                    brandMap.put("id", row[10]);
                    brandMap.put("name", row[11]);
                } else {
                    brandMap = null;
                }
                productMap.put("brand", brandMap);
                productMap.put("images", new ArrayList<Map<String, Object>>());

                Map<String, Object> categoryMap = new HashMap<>();
                if (row[15] != null) {
                    categoryMap.put("id", row[15]);
                    categoryMap.put("name", row[16]);

                    Map<String, Object> parentCategoryMap = new HashMap<>();
                    if (row[17] != null) {
                        parentCategoryMap.put("id", row[17]);
                        parentCategoryMap.put("name", row[18]);
                    } else {
                        parentCategoryMap = null;
                    }
                    categoryMap.put("parentCategory", parentCategoryMap);
                } else {
                    categoryMap = null;
                }
                productMap.put("category", categoryMap);

                productsMap.put(productId, productMap);
            }

            if (row[12] != null) {
                Map<String, Object> imageMap = new HashMap<>();
                imageMap.put("id", row[12]);
                imageMap.put("url", row[13]);
                imageMap.put("isThumbnail", row[14]);
                ((List<Map<String, Object>>) productMap.get("images")).add(imageMap);
            }
        }

        return new ArrayList<>(productsMap.values());
    }


    @Transactional
    public Product createProduct(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("User not found"));
        Shop shop = user.getShop();

        Brand brand = null;
        JsonNode brandNode = json.get("brand");
        if (brandNode != null && !brandNode.isNull()) {
            Long brandId = brandNode.get("id").asLong();
            brand = brandService.findById(brandId);
        }

        Category category = null;
        JsonNode categoryNode = json.get("category");
        if (categoryNode != null && !categoryNode.isNull()) {
            Long categoryId = categoryNode.get("id").asLong();
            category = categoryService.findById(categoryId);
        }

        Product product = new Product();
        product.setName(json.get("name").asText());
        product.setDescription(json.get("description").asText());
        product.setSalesPrice(json.get("salesPrice").asDouble());
        product.setGrossPrice(json.get("grossPrice").asDouble());
        product.setVatRate(json.get("vatRate").asDouble());
        product.setCode(json.get("code").asText());
        product.setGtin(json.get("gtin").asText());
        product.setStock(json.get("stock").asInt());
        product.setActive(json.get("active").asBoolean());
        product.setShop(shop);
        product.setBrand(brand);
        product.setCategory(category);
        product.setDateCreated(new Date());

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
    public Product updateProductById(Long productId, JsonNode json) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product could not be found by id: " + productId));

        JsonNode brandNode = json.get("brand");
        if (brandNode != null && !brandNode.isNull()) {
            Long brandId = brandNode.get("id").asLong();
            Brand brand = brandService.findById(brandId);
            product.setBrand(brand);
        }

        JsonNode categoryNode = json.get("category");
        if (categoryNode != null && !categoryNode.isNull()) {
            Long categoryId = categoryNode.get("id").asLong();
            Category category = categoryService.findById(categoryId);
            product.setCategory(category);
        }

        product.setName(json.get("name").asText());
        product.setDescription(json.get("description").asText());
        product.setSalesPrice(json.get("salesPrice").asDouble());
        product.setGrossPrice(json.get("grossPrice").asDouble());
        product.setVatRate(json.get("vatRate").asDouble());
        product.setCode(json.get("code").asText());
        product.setGtin(json.get("gtin").asText());
        product.setStock(json.get("stock").asInt());
        product.setActive(json.get("active").asBoolean());

        if (json.has("images")) {
            List<Image> currentImages = product.getImages();
            Map<Long, Image> currentImageMap = currentImages.stream()
                    .collect(Collectors.toMap(Image::getId, image -> image));

            List<Image> updatedImages = new ArrayList<>();

            for (JsonNode imageNode : json.get("images")) {
                Long imageId = imageNode.has("id") ? imageNode.get("id").asLong() : null;
                Image image;

                if (imageId != null && currentImageMap.containsKey(imageId)) {
                    image = currentImageMap.get(imageId);
                    image.setIsThumbnail(imageNode.get("isThumbnail").asBoolean());
                } else {
                    image = new Image();
                    image.setUrl(imageNode.get("url").asText());
                    image.setIsThumbnail(imageNode.get("isThumbnail").asBoolean());
                    image = imageRepository.save(image);
                }
                updatedImages.add(image);
            }

            currentImages.clear();
            currentImages.addAll(updatedImages);
        }

        return productRepository.save(product);
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

//    public Product findProductById(Long id) {
//        Product product = productRepository.findById(id).orElseThrow(()
//                -> new RuntimeException("Product not found by id: " + id));
//
//        return product;
//    }

    public Map<String, Object> findProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", product.getId());
        productMap.put("name", product.getName());
        productMap.put("description", product.getDescription());
        productMap.put("salesPrice", product.getSalesPrice());
        productMap.put("grossPrice", product.getGrossPrice());
        productMap.put("vatRate", product.getVatRate());
        productMap.put("code", product.getCode());
        productMap.put("gtin", product.getGtin());
        productMap.put("stock", product.getStock());
        productMap.put("active", product.isActive());

        Map<String, Object> brandMap = new HashMap<>();
        Brand brand = product.getBrand();
        if (brand != null) {
            brandMap.put("id", brand.getId());
            brandMap.put("name", brand.getName());
        } else {
            brandMap = null;
        }
        productMap.put("brand", brandMap);

        List<Map<String, Object>> imagesList = new ArrayList<>();
        for (Image image : product.getImages()) {
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("id", image.getId());
            imageMap.put("url", image.getUrl());
            imageMap.put("isThumbnail", image.getIsThumbnail());
            imagesList.add(imageMap);
        }
        productMap.put("images", imagesList);

        Map<String, Object> categoryMap = new HashMap<>();
        Category category = product.getCategory();
        if (category != null) {
            categoryMap.put("id", category.getId());
            categoryMap.put("name", category.getName());

            Map<String, Object> parentCategoryMap = new HashMap<>();
            Category parentCategory = category.getParentCategory();
            if (parentCategory != null) {
                parentCategoryMap.put("id", parentCategory.getId());
                parentCategoryMap.put("name", parentCategory.getName());
            } else {
                parentCategoryMap = null;
            }
            categoryMap.put("parentCategory", parentCategoryMap);
        } else {
            categoryMap = null;
        }
        productMap.put("category", categoryMap);

        return productMap;
    }
}
