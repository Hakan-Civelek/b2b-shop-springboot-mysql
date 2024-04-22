package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateBasketRecord;
import com.b2bshop.project.model.Basket;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.BasketRepository;
import com.b2bshop.project.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
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

    public BasketService(BasketRepository basketRepository, SecurityService securityService, JwtService jwtService,
                         UserRepository userRepository) {
        this.basketRepository = basketRepository;
        this.securityService = securityService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> getAllBaskets(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        Optional<User> user = userRepository.findByUsername(userName);
        Long userId = user.get().getId();

        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT " +
                " basket.id basketId, " +
                " product.id as productId, product.name as name, basket.quantity as quantity, " +
                " product.grossPrice as grossPrice," +
                " (product.grossPrice * basket.quantity) as totalProductPrice " +
                " FROM Basket basket " +
                " JOIN basket.product as product " +
                " JOIN basket.user as user " +
                " WHERE  1 = 1 ";

        if (userId != null) {
            hqlQuery += " AND user.id = :userId ";
        }

        Query query = session.createQuery(hqlQuery);

        if (userId != null) {
            query.setParameter("userId", userId);
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Object[]> rows = query.list();

        for (Object[] row : rows) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("basketId", row[0]);
            resultMap.put("totalProductPrice", row[5]);

            Map<String, Object> productObject = new HashMap<>();
            productObject.put("id", row[1]);
            productObject.put("name", row[2]);
            productObject.put("quantity", row[3]);
            productObject.put("grossPrice", row[4]);

            resultMap.put("product", productObject);

            resultList.add(resultMap);
        }
        return resultList;
    }

    public Basket createBasket(CreateBasketRecord request) {
        Basket newBasket = Basket.builder()
                .user(request.user())
                .product(request.product())
                .quantity(request.quantity())
                .build();

        return basketRepository.save(newBasket);
    }

    public Basket updateBasketById(Long basketId, Basket newBasket) {
        Optional<Basket> basket = basketRepository.findById(basketId);
        if (basket.isPresent()) {
            Basket oldProduct = basket.get();
            oldProduct.setUser(newBasket.getUser());
            oldProduct.setProduct(newBasket.getProduct());
            oldProduct.setQuantity(newBasket.getQuantity());
            basketRepository.save(oldProduct);
            return oldProduct;
        }
        return null;
    }
}
