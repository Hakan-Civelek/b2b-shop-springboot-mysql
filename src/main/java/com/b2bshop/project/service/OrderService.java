package com.b2bshop.project.service;

import com.b2bshop.project.model.*;
import com.b2bshop.project.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderService {
    private final ProductRepository productRepository;
    private final SecurityService securityService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final BasketRepository basketRepository;

    public OrderService(ProductRepository productRepository, SecurityService securityService, JwtService jwtService, UserRepository userRepository,
                        CustomerRepository customerRepository,
                        OrderRepository orderRepository, BasketRepository basketRepository) {
        this.productRepository = productRepository;
        this.securityService = securityService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.basketRepository = basketRepository;
    }

    @Transactional
    public List<Map<String, Object>> getAllOrders(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);

        Session session = entityManager.unwrap(Session.class);
        String orderQuery = "SELECT order.id as orderId, order.orderNote as orderNote," +
                " order.totalPrice as totalPrice, order.withoutTaxPrice as withoutTaxPrice," +
                " order.totalTax as totalTax, " +
                " createdBy.id as createdById, createdBy.name as createdByName, " +
                " order.orderNumber as orderNumber,  order.orderDate as orderDate," +
                " orderItem.refProductId as orderItemRefProductId, orderItem.name as orderItemName, " +
                " orderItem.salesPrice as orderItemSalesPrice, orderItem.grossPrice as orderItemGrossPrice," +
                " orderItem.quantity as orderItemQuantity" +
                " FROM Order as order " +
                " JOIN order.customer as customer ON customer.tenantId = :tenantId" +
                " JOIN order.createdBy as createdBy " +
                " JOIN order.orderItems as orderItem " +
                " WHERE  1 = 1 ";

        Query query = session.createQuery(orderQuery);

        query.setParameter("tenantId", tenantId);

        List<Map<String, Object>> orderResultList = new ArrayList<>();
        Map<Long, Map<String, Object>> orderMap = new HashMap<>();
        List<Object[]> orderRows = query.list();

        for (Object[] orderRow : orderRows) {
            Long orderId = (Long) orderRow[0];
            Map<String, Object> orderItemMap = orderMap.getOrDefault(orderId, new HashMap<>());
            orderItemMap.put("orderId", orderId);
            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderItemMap.getOrDefault("orderItems", new ArrayList<>());

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("orderId", orderRow[0]);
            resultMap.put("orderNote", orderRow[1]);
            resultMap.put("totalPrice", orderRow[2]);
            resultMap.put("withoutTaxPrice", orderRow[3]);
            resultMap.put("totalTax", orderRow[4]);
            resultMap.put("createdById", orderRow[5]);
            resultMap.put("createdByName", orderRow[6]);
            resultMap.put("orderNumber", orderRow[7]);
            resultMap.put("orderDate", orderRow[8]);

            orderItemMap.put("refProductId", orderRow[9]);
            orderItemMap.put("name", orderRow[10]);
            orderItemMap.put("salesPrice", orderRow[11]);
            orderItemMap.put("grossPrice", orderRow[12]);
            orderItemMap.put("quantity", orderRow[13]);

            orderItems.add(resultMap);
            orderItemMap.put("orderItems", orderItems);
            orderMap.put(orderId, orderItemMap);
        }
        orderResultList.addAll(orderMap.values());

        return orderResultList;
    }

    @Transactional
    public Order createOrder(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);

        Order order = new Order();
        String orderNumber = generateOrderNumber(tenantId);
        String orderNote = json.get("orderNote").asText();

        String userName = jwtService.extractUser(token);
        order.setCustomer(customerRepository.findById(tenantId).orElseThrow(()
                -> new RuntimeException("Customer not found")));
        order.setOrderNumber(orderNumber);
        order.setOrderNote(orderNote);
        order.setOrderItems(new ArrayList<>());
        order.setOrderDate(new Date());
        order.setCreatedBy(userRepository.findByUsername(userName).orElseThrow(()
                -> new RuntimeException("User not found")));

        JsonNode orderItems = json.get("orderItems");
        Double totalPrice = 0.0;
        Double withoutTaxPrice = 0.0;
        Double totalTax = 0.0;
        for (JsonNode orderItemNode : orderItems) {
            totalPrice += orderItemNode.get("grossPrice").asInt();
            withoutTaxPrice += orderItemNode.get("salesPrice").asInt();
            totalTax = totalPrice - withoutTaxPrice;

            OrderItem orderItem = OrderItem.builder()
                    .refProductId(orderItemNode.get("productId").asLong())
                    .name(orderItemNode.get("productName").toString())
                    .salesPrice((orderItemNode.get("salesPrice")).asDouble())
                    .grossPrice((orderItemNode.get("grossPrice")).asDouble())
                    .quantity((orderItemNode.get("quantity")).asInt())
                    .build();

            basketRepository.deleteById(json.get("basketId").asLong());
            order.getOrderItems().add(orderItem);
        }
        order.setTotalPrice(totalPrice);
        order.setWithoutTaxPrice(withoutTaxPrice);
        order.setTotalTax(totalTax);
        return orderRepository.save(order);
    }

    public static String generateOrderNumber(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String timestamp = now.format(formatter);

        String orderNumber = tenantId.toString() + timestamp.substring(0, 8);

        return orderNumber;
    }
}
