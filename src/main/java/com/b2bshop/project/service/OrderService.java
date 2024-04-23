package com.b2bshop.project.service;

import com.b2bshop.project.model.Order;
import com.b2bshop.project.model.OrderDetail;
import com.b2bshop.project.model.Product;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.*;
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
public class OrderService {
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final SecurityService securityService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public OrderService(OrderDetailRepository orderDetailRepository, ProductRepository productRepository, SecurityService securityService, JwtService jwtService, UserRepository userRepository,
                        CustomerRepository customerRepository,
                        OrderRepository orderRepository) {
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.securityService = securityService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public List<Map<String, Object>> getAllOrders(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);

        Session session = entityManager.unwrap(Session.class);
        String orderQuery = "SELECT order.id as orderId, order.orderNote as orderNote," +
                " order.totalPrice as totalPrice, order.withoutTaxPrice as withoutTaxPrice," +
                " order.totalTax as totalTax, " +
                " createdBy.id as createdById, createdBy.name as createdByName "+
                " FROM Order as order " +
                " JOIN order.customer as customer ON customer.tenantId = :tenantId" +
                " JOIN order.createdBy as createdBy" +
                " WHERE  1 = 1 ";

        Query query = session.createQuery(orderQuery);

        query.setParameter("tenantId", tenantId);

        List<Map<String, Object>> orderResultList = new ArrayList<>();
        List<Object[]> orderRows = query.list();

        String materialQuery = "SELECT order.id as orderId, order.orderNote as orderNote," +
                " order.totalPrice as totalPrice, order.withoutTaxPrice as withoutTaxPrice," +
                " order.totalTax as totalTax, " +
                " createdBy.id as createdById, createdBy.name as createdByName "+
                " FROM Order as order " +
                " JOIN order.customer as customer ON customer.tenantId = :tenantId" +
                " JOIN order.createdBy as createdBy" +
                " WHERE  1 = 1 ";
        Query query1 = session.createQuery(orderQuery);

        query.setParameter("tenantId", tenantId);

        List<Map<String, Object>> resultList1 = new ArrayList<>();
        List<Object[]> rows = query.list();

        for (Object[] orderRow : orderRows) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("orderId", orderRow[0]);
            resultMap.put("orderNote", orderRow[1]);
            resultMap.put("totalPrice", orderRow[2]);
            resultMap.put("withoutTaxPrice", orderRow[3]);
            resultMap.put("totalTax", orderRow[4]);
            resultMap.put("createdById", orderRow[5]);
            resultMap.put("createdByName", orderRow[6]);

//            Map<String, Object> productObject = new HashMap<>();
//            productObject.put("id", row[1]);
//            productObject.put("name", row[2]);
//            productObject.put("quantity", row[3]);
//            productObject.put("grossPrice", row[4]);
//            productObject.put("salesPrice", row[5]);

//            resultMap.put("product", productObject);

            orderResultList.add(resultMap);
        }
        return orderResultList;
    }

    @Transactional
    public Order createOrder(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);
        String userName = jwtService.extractUser(token);
        Optional<User> user = userRepository.findByUsername(userName);
        List<Long> productIds = new ArrayList<>();
        List<Product> products = productRepository.findAllById(productIds);
        Order order = new Order();
        String orderNote = json.get("orderNote").asText();

        JsonNode baskets = json.get("baskets");
        Double withoutTaxPrice = 0.0;
        Double totalTax = 0.0;
        Double totalPrice = 0.0;
        for (JsonNode basket : baskets) {
            JsonNode productNode = basket.get("product");
            Product product = productRepository.findById(productNode.path("id").asLong())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            System.out.println("basket.path(\"totalGrossPrice\").asLong() " + basket.path("totalGrossPrice"));
            totalPrice += basket.path("totalGrossPrice").asDouble();
            withoutTaxPrice += basket.path("totalSalesPrice").asDouble();
            totalTax = totalPrice - withoutTaxPrice;

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(product);
            orderDetail.setQuantity(productNode.path("quantity").asInt());
            orderDetailRepository.save(orderDetail);
        }
        order.setCustomer(customerRepository.findById(tenantId).orElseThrow(()
                -> new RuntimeException("Customer not found")));
        order.setOrderNote(orderNote);
        order.setProducts(products);
        order.setOrderDate(new Date());
        order.setCreatedBy(user.orElseThrow(()
                -> new RuntimeException("User not found")));
        order.setTotalPrice(totalPrice);
        order.setWithoutTaxPrice(withoutTaxPrice);
        order.setTotalTax(totalTax);
        orderRepository.save(order);
        return order;
    }
}
