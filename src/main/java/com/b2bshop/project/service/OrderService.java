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
    private final SecurityService securityService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final BasketRepository basketRepository;
    private final AddressRepository addressRepository;

    public OrderService(SecurityService securityService, JwtService jwtService, UserRepository userRepository,
                        CustomerRepository customerRepository, OrderRepository orderRepository,
                        BasketRepository basketRepository,
                        AddressRepository addressRepository) {
        this.securityService = securityService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.basketRepository = basketRepository;
        this.addressRepository = addressRepository;
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
                " orderItem.quantity as orderItemQuantity, orderItem.id as orderItemId," +
                " invoiceAddress as invoiceAddressMap, receiverAddress as receiverAddressMap " +
                " FROM Order as order " +
                " JOIN order.customer as customer ON customer.tenantId = :tenantId" +
                " JOIN order.createdBy as createdBy " +
                " JOIN order.orderItems as orderItem " +
                " JOIN order.invoiceAddress as invoiceAddress " +
                " JOIN order.receiverAddress as receiverAddress " +
                " WHERE  1 = 1 ";

        Query query = session.createQuery(orderQuery);

        query.setParameter("tenantId", tenantId);

        List<Map<String, Object>> orderResultList = new ArrayList<>();
        Map<Long, Map<String, Object>> orderResultMap = new HashMap<>();
        List<Object[]> orderRows = query.list();

        for (Object[] orderRow : orderRows) {
            Long orderId = (Long) orderRow[0];
            Map<String, Object> orderMap = orderResultMap.getOrDefault(orderId, new HashMap<>());
            orderMap.put("orderId", orderId);
            orderMap.put("orderNumber", orderRow[7]);
            orderMap.put("orderNote", orderRow[1]);
            orderMap.put("orderDate", orderRow[8]);
            orderMap.put("createdById", orderRow[5]);
            orderMap.put("createdByName", orderRow[6]);
            orderMap.put("totalPrice", orderRow[2]);
            orderMap.put("withoutTaxPrice", orderRow[3]);
            orderMap.put("totalTax", orderRow[4]);

            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderMap.getOrDefault("orderItems", new ArrayList<>());

            Map<String, Object> orderItem = new HashMap<>();
            orderItem.put("name", orderRow[10]);
            orderItem.put("grossPrice", orderRow[12]);
            orderItem.put("salesPrice", orderRow[11]);
            orderItem.put("quantity", orderRow[13]);
            orderItem.put("refProductId", orderRow[9]);
            orderItem.put("id", orderRow[14]);

            orderItems.add(orderItem);
            orderMap.put("orderItems", orderItems);
            orderMap.put("invoiceAddress", orderRow[15]);
            orderMap.put("receiverAddress", orderRow[16]);

            orderResultMap.put(orderId, orderMap);
        }

        orderResultList.addAll(orderResultMap.values());
        return orderResultList;
    }

    @Transactional
    public Order createOrder(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);
        Long invoiceAddressId = json.get("invoiceAddressId").asLong();
        Long receiverAddressId = json.get("receiverAddressId").asLong();

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
        order.setInvoiceAddress(addressRepository.findById(invoiceAddressId).orElseThrow(()
                -> new RuntimeException("Address not found")));
        order.setReceiverAddress(addressRepository.findById(receiverAddressId).orElseThrow(()
                -> new RuntimeException("Address not found")));

        JsonNode orderItems = json.get("orderItems");
        Double totalPrice = 0.0;
        Double withoutTaxPrice = 0.0;
        Double totalTax = 0.0;
        for (JsonNode orderItemNode : orderItems) {
            totalPrice += orderItemNode.get("grossPrice").asDouble() * (orderItemNode.get("quantity").asDouble());
            withoutTaxPrice += orderItemNode.get("salesPrice").asDouble() * (orderItemNode.get("quantity").asDouble());
            totalTax = totalPrice - withoutTaxPrice;

            OrderItem orderItem = OrderItem.builder()
                    .refProductId(orderItemNode.get("productId").asLong())
                    .name(orderItemNode.get("productName").asText())
                    .salesPrice(((orderItemNode.get("salesPrice")).asDouble()))
                    .grossPrice((orderItemNode.get("grossPrice")).asDouble())
                    .quantity((orderItemNode.get("quantity")).asInt())
                    .build();

            basketRepository.deleteById(json.get("basketId").asLong());
            order.getOrderItems().add(orderItem);
        }
        order.setTotalPrice(totalPrice);
        order.setWithoutTaxPrice(withoutTaxPrice);
        order.setTotalTax(totalTax);
//        return null;
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
