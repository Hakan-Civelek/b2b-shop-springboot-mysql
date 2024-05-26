package com.b2bshop.project.service;

import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.Basket;
import com.b2bshop.project.model.BasketItem;
import com.b2bshop.project.model.Product;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.BasketItemRepository;
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
    private final UserService userService;

    private final BasketItemRepository basketItemRepository;

    public BasketService(BasketRepository basketRepository, JwtService jwtService,
                         UserRepository userRepository, EntityManager entityManager,
                         ProductService productService, UserService userService, BasketItemRepository basketItemRepository) {
        this.basketRepository = basketRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.productService = productService;
        this.entityManager = entityManager;
        this.userService = userService;
        this.basketItemRepository = basketItemRepository;
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
                " product.grossPrice AS grossPrice, product.salesPrice AS salesPrice, " +
                " image.id AS imageId, image.url AS imageUrl, image.isThumbnail AS imageIsThumbnail ," +
                " product.stock AS productStock " +
                " FROM Basket as basket " +
                " JOIN basket.basketItems as basketItem " +
                " JOIN basketItem.product as product " +
                " LEFT JOIN product.images as image " +
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

            Map<Long, Map<String, Object>> basketItemsMap = new HashMap<>();
            for (Object[] row : rows) {
                Long basketItemId = (Long) row[1];

                Map<String, Object> basketItem = basketItemsMap.getOrDefault(basketItemId, new HashMap<>());
                if (!basketItem.containsKey("basketItemId")) {
                    basketItem.put("basketItemId", basketItemId);
                    basketItem.put("productId", row[2]);
                    basketItem.put("productStock", row[10]);
                    basketItem.put("productName", row[3]);
                    basketItem.put("quantity", row[4]);
                    basketItem.put("grossPrice", row[5]);
                    basketItem.put("salesPrice", row[6]);
                    basketItem.put("images", new ArrayList<Map<String, Object>>());
                    basketItemsMap.put(basketItemId, basketItem);
                }

                List<Map<String, Object>> images = (List<Map<String, Object>>) basketItem.get("images");
                Map<String, Object> image = new HashMap<>();
                image.put("id", row[7]);
                image.put("url", row[8]);
                image.put("isThumbnail", row[9]);
                images.add(image);
            }

            basketMap.put("basketItems", new ArrayList<>(basketItemsMap.values()));
        }

        return basketMap;
    }

    @Transactional
    public Basket addItemOnBasket(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userService.findUserByName(userName);
        Long userId = user.getId();
        Basket basket;
        Optional<Basket> optionalBasket = basketRepository.findByUserId(userId);
        if (optionalBasket.isPresent()) {
            basket = optionalBasket.get();
        } else {
            basket = new Basket();
            basket.setUser(user);
        }
        List<BasketItem> basketItems = basket.getBasketItems();

        boolean itemExists = false;
        long productId = json.get("productId").asLong();
        int quantity = json.get("quantity").asInt();
        boolean updateQuantity = json.get("updateQuantity").asBoolean();

        if (basketItems == null) {
            basketItems = new ArrayList<>();
            basket.setBasketItems(basketItems);
        }

        for (BasketItem item : basket.getBasketItems()) {
            if (item.getProduct().getId() == productId) {
                if (updateQuantity) {
                    item.setQuantity(quantity);
                } else {
                    item.setQuantity(item.getQuantity() + quantity);
                }
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
            basket.getBasketItems().add(newItem);
        }

        return basketRepository.save(basket);
    }

    public Basket findBasketById(Long id) {
        return basketRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Basket could not find by id: " + id));
    }

    public Basket removeItem(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userService.findUserByName(userName);
        Long userId = user.getId();
        Basket basket;
        Optional<Basket> optionalBasket = basketRepository.findByUserId(userId);
        if (optionalBasket.isPresent()) {
            basket = optionalBasket.get();
        } else
            throw new ResourceNotFoundException("Basket could not find!");

        List<BasketItem> basketItems = basket.getBasketItems();

        long productId = json.get("productId").asLong();

        if (basketItems != null) {
            Iterator<BasketItem> iterator = basketItems.iterator();
            while (iterator.hasNext()) {
                BasketItem item = iterator.next();
                if (item.getProduct().getId() == productId) {
                    iterator.remove();
                    break;
                }
            }
        }

        return basketRepository.save(basket);
    }

    @Transactional
    public Map<String, String> cleanBasket(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userService.findUserByName(userName);
        Long userId = user.getId();
        Basket basket;
        Optional<Basket> optionalBasket = basketRepository.findByUserId(userId);
        if (optionalBasket.isPresent()) {
            basket = optionalBasket.get();
            List<BasketItem> basketItems = basket.getBasketItems();

            for (BasketItem item : basketItems) {
                basketItemRepository.deleteById(item.getId());
            }

            basketItems.clear();
        } else {
            throw new ResourceNotFoundException("Basket could not find for this user! UserId:" + user.getId());
        }

        Map<String, String> response = new HashMap<>();
        response.put("success", "true");

        basketRepository.save(basket);

        return response;
    }

}
