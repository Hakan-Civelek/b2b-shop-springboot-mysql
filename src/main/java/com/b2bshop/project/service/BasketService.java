package com.b2bshop.project.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BasketService {
    @Autowired
    private EntityManager entityManager;
    private final BasketRepository basketRepository;
    private final SecurityService securityService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public BasketService(BasketRepository basketRepository, SecurityService securityService, JwtService jwtService,
                         UserRepository userRepository,
                         ProductRepository productRepository) {
        this.basketRepository = basketRepository;
        this.securityService = securityService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
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
            Map<String, Object> basketItemMap = basketMap.getOrDefault(basketId, new HashMap<>());
            basketItemMap.put("basketId", basketId);
            List<Map<String, Object>> basketItems = (List<Map<String, Object>>) basketItemMap.getOrDefault("basketItems", new ArrayList<>());

            Map<String, Object> basketItemsMap = new HashMap<>();
            basketItemsMap.put("basketItemId", row[1]);
            basketItemsMap.put("productId", row[2]);
            basketItemsMap.put("productName", row[3]);
            basketItemsMap.put("quantity", row[4]);
            basketItemsMap.put("grossPrice", row[5]);
            basketItemsMap.put("salesPrice", row[6]);

            basketItems.add(basketItemsMap);
            basketItemMap.put("basketItems", basketItems);
            basketMap.put(basketId, basketItemMap);
        }

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

//        List<BasketItem> basketItems = new ArrayList<>();
        for (JsonNode itemNode : basketItemsNode) {
            JsonNode productNode = itemNode.get("product");
            int productId = productNode.get("id").asInt();
            int quantity = itemNode.get("quantity").asInt();

            BasketItem basketItem = BasketItem.builder()
                    .product(productRepository.findById((long) productId).orElse(null))
                    .quantity(quantity)
                    .build();

            basket.getBasketItems().add(basketItem);
        }

        return basketRepository.save(basket);
    }

    public Basket updateBasketById(Long basketId, Basket newBasket) {
        Optional<Basket> basket = basketRepository.findById(basketId);
        if (basket.isPresent()) {
            Basket oldProduct = basket.get();
            oldProduct.setUser(newBasket.getUser());
            oldProduct.setBasketItems(newBasket.getBasketItems());
            basketRepository.save(oldProduct);
            return oldProduct;
        }
        return null;
    }
}
