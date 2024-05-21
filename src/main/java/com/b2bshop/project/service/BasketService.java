package com.b2bshop.project.service;

import com.b2bshop.project.exception.BasketNotFoundException;
import com.b2bshop.project.model.Basket;
import com.b2bshop.project.model.BasketItem;
import com.b2bshop.project.model.Product;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.BasketRepository;
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
public class BasketService {
    private final EntityManager entityManager;
    private final BasketRepository basketRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ProductService productService;

    public BasketService(BasketRepository basketRepository, JwtService jwtService,
                         UserRepository userRepository, EntityManager entityManager,
                         ProductService productService) {
        this.basketRepository = basketRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.productService = productService;
        this.entityManager = entityManager;
    }

    public Map<String, Object> getBasket(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        Optional<User> user = userRepository.findByUsername(userName);
        Long userId = user.get().getId();

        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT " +
                " basket.id AS basketId, basketItem.id AS basketItemId, " +
                " product.id AS productId, product.name AS productName, " +
                " basketItem.quantity AS quantity, " +
                " product.grossPrice AS grossPrice, product.salesPrice AS salesPrice " +
                " FROM Basket as basket " +
                " JOIN basket.basketItems as basketItem " +
                " JOIN basketItem.product as product " +
                " WHERE basket.user.id = :userId";

        Query query = session.createQuery(hqlQuery);

        if (userId != null) {
            query.setParameter("userId", userId);
        }

        Map<String, Object> basketMap = new HashMap<>();
        List<Object[]> rows = query.list();

        if (!rows.isEmpty()) {
            Object[] firstRow = rows.get(0);
            Long basketId = (Long) firstRow[0];
            basketMap.put("id", basketId);

            List<Map<String, Object>> basketItems = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> basketItem = new HashMap<>();
                basketItem.put("basketItemId", row[1]);
                basketItem.put("productId", row[2]);
                basketItem.put("productName", row[3]);
                basketItem.put("quantity", row[4]);
                basketItem.put("grossPrice", row[5]);
                basketItem.put("salesPrice", row[6]);

                basketItems.add(basketItem);
            }
            basketMap.put("basketItems", basketItems);
        }

        return basketMap;
    }

    @Transactional
    public Basket createBasket(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        Optional<User> user = userRepository.findByUsername(userName);
        Long userId = user.get().getId();

        JsonNode basketItemsNode = json.get("basketItems");

        Basket basket = new Basket();
        basket.setUser(userRepository.findById(userId).orElse(null));
        basket.setBasketItems(new ArrayList<>());

        long productId = json.get("productId").asLong();
        int quantity = json.get("quantity").asInt();
        boolean isStockAvailable = productService.checkStockById(productId, quantity);

        if (isStockAvailable) {
            BasketItem basketItem = BasketItem.builder()
                    .product(productService.findProductById((productId)))
                    .quantity(quantity)
                    .build();
            basket.getBasketItems().add(basketItem);
        } else
            throw new RuntimeException("Stock is not enough for material: " + productService.findProductById(productId).getName());

        return basketRepository.save(basket);
    }

    @Transactional
    public Basket updateBasket(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        Optional<User> user = userRepository.findByUsername(userName);
        Long userId = user.get().getId();
        Basket basket = basketRepository.findByUserId(userId).orElseThrow(null);
        List<BasketItem> basketItems = basket.getBasketItems();

        boolean itemExists = false;
        long productId = json.get("productId").asLong();
        int quantity = json.get("quantity").asInt();

        for (BasketItem item : basketItems) {
            if (item.getProduct().getId() == productId) {
                item.setQuantity(quantity);
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            Product product = productService.findProductById(productId);

            BasketItem newItem = BasketItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .build();
            basketItems.add(newItem);

        }
        return basketRepository.save(basket);
    }

    public Basket findBasketById(Long id) {
        return basketRepository.findById(id).orElseThrow(()
                -> new BasketNotFoundException("Basket could not find by id: " + id));
    }
}
