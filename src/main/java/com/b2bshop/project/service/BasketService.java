package com.b2bshop.project.service;

import com.b2bshop.project.exception.BasketNotFoundException;
import com.b2bshop.project.model.Basket;
import com.b2bshop.project.model.BasketItem;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.BasketRepository;
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
public class BasketService {
    private final EntityManager entityManager;
    private final BasketRepository basketRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public BasketService(BasketRepository basketRepository, JwtService jwtService,
                         UserRepository userRepository, EntityManager entityManager,
                         ProductRepository productRepository, ProductService productService) {
        this.basketRepository = basketRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.entityManager = entityManager;
    }

    public List<Map<String, Object>> getAllBaskets(HttpServletRequest request) {
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

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<Long, Map<String, Object>> basketMap = new HashMap<>();

        List<Object[]> rows = query.list();

        for (Object[] row : rows) {
            Long basketId = (Long) row[0];
            Map<String, Object> basketItemsMap = basketMap.getOrDefault(basketId, new HashMap<>());
            basketItemsMap.put("basketId", basketId);
            List<Map<String, Object>> basketItems = (List<Map<String, Object>>) basketItemsMap.getOrDefault("basketItems", new ArrayList<>());

            Map<String, Object> basketItem = new HashMap<>();
            basketItem.put("basketItemId", row[1]);
            basketItem.put("productId", row[2]);
            basketItem.put("productName", row[3]);
            basketItem.put("quantity", row[4]);
            basketItem.put("grossPrice", row[5]);
            basketItem.put("salesPrice", row[6]);

            basketItems.add(basketItem);
            basketItemsMap.put("basketItems", basketItems);
            basketMap.put(basketId, basketItemsMap);
        }

        System.out.println("basketMap: " + basketMap.toString());
        resultList.addAll(basketMap.values());

        return resultList;
    }

    @Transactional
    public Basket createBasket(HttpServletRequest request, JsonNode json) {
        System.out.println("json: " + json);

        JsonNode userNode = json.get("user");
        JsonNode basketItemsNode = json.get("basketItems");

        int userId = userNode.get("id").asInt();

        Basket basket = new Basket();
        basket.setUser(userRepository.findById((long) userId).orElse(null));
        basket.setBasketItems(new ArrayList<>());

        for (JsonNode itemNode : basketItemsNode) {
            JsonNode productNode = itemNode.get("product");
            long productId = productNode.get("id").asLong();
            int quantity = itemNode.get("quantity").asInt();
            boolean isStockAvailable = productService.checkStockById(productId, quantity);

            if (isStockAvailable) {
                BasketItem basketItem = BasketItem.builder()
                        .product(productService.findProductById((productId)))
                        .quantity(quantity)
                        .build();
                basket.getBasketItems().add(basketItem);
            } else
                throw new RuntimeException("Stock is not enough for material: " + productService.findProductById(productId).getName());
        }

        return basketRepository.save(basket);
    }

    public Basket updateBasketById(Long basketId, Basket newBasket) {
        Basket basket = findBasketById(basketId);

        basket.setUser(newBasket.getUser());
        basket.setBasketItems(newBasket.getBasketItems());

        basketRepository.save(basket);
        return basket;
    }

    public Basket findBasketById(Long id) {
        return basketRepository.findById(id).orElseThrow(()
                -> new BasketNotFoundException("Basket could not find by id: " + id));
    }
}
