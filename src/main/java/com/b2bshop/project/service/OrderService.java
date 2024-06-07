package com.b2bshop.project.service;

import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.*;
import com.b2bshop.project.repository.*;
import com.b2bshop.project.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderService {
    private final SecurityService securityService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final CustomerService customerService;
    private final OrderRepository orderRepository;
    private final BasketRepository basketRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final AddressService addressService;

    private final BasketService basketService;

    public OrderService(SecurityService securityService, JwtService jwtService, UserRepository userRepository,
                        OrderRepository orderRepository, BasketRepository basketRepository, EntityManager entityManager,
                        CustomerService customerService, ProductService productService,
                        ProductRepository productRepository, AddressService addressService, BasketService basketService) {
        this.securityService = securityService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.basketRepository = basketRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.productRepository = productRepository;
        this.entityManager = entityManager;
        this.addressService = addressService;
        this.basketService = basketService;
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
                " invoiceAddress as invoiceAddressMap, receiverAddress as receiverAddressMap, " +
                " image.id as imageId, image.url as imageUrl, image.isThumbnail as imageIsThumbnail, " +
                " order.orderStatus as orderStatusId " +
                " FROM Order as order " +
                " JOIN order.customer as customer ON customer.tenantId = :tenantId" +
                " JOIN order.createdBy as createdBy " +
                " JOIN order.orderItems as orderItem " +
                " LEFT JOIN orderItem.images as image " +
                " JOIN order.invoiceAddress as invoiceAddress " +
                " JOIN order.receiverAddress as receiverAddress " +
                " WHERE  1 = 1 " +
                " ORDER BY order.id DESC ";

        Query query = session.createQuery(orderQuery);
        query.setParameter("tenantId", tenantId);

        List<Map<String, Object>> orderResultList = new ArrayList<>();
        Map<Long, Map<String, Object>> orderResultMap = new LinkedHashMap<>();
        List<Object[]> orderRows = query.list();

        for (Object[] orderRow : orderRows) {
            Long orderId = (Long) orderRow[0];
            Long orderItemId = (Long) orderRow[14];
            Long imageId = (Long) orderRow[17];

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

            OrderStatus orderStatus = (OrderStatus) orderRow[20];
            Map<String, Object> orderStatusMap = new HashMap<>();
            orderStatusMap.put("id", orderStatus.getId());
            orderStatusMap.put("status", orderStatus.getStatus());
            orderMap.put("orderStatus", orderStatusMap);

            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderMap.getOrDefault("orderItems", new ArrayList<>());

            Map<String, Object> orderItem = orderItems.stream()
                    .filter(item -> item.get("id").equals(orderItemId))
                    .findFirst()
                    .orElse(new HashMap<>());

            if (!orderItem.containsKey("id")) {
                orderItem.put("name", orderRow[10]);
                orderItem.put("grossPrice", orderRow[12]);
                orderItem.put("salesPrice", orderRow[11]);
                orderItem.put("quantity", orderRow[13]);
                orderItem.put("refProductId", orderRow[9]);
                orderItem.put("id", orderItemId);
                orderItem.put("images", new ArrayList<Map<String, Object>>());
                orderItems.add(orderItem);
            }

            List<Map<String, Object>> images = (List<Map<String, Object>>) orderItem.get("images");
            Map<String, Object> image = new HashMap<>();
            image.put("id", imageId);
            image.put("url", orderRow[18]);
            image.put("isThumbnail", orderRow[19]);
            images.add(image);

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
        Long basketId = json.get("basketId").asLong();

        Order order = new Order();
        String orderNumber = generateOrderNumber(tenantId);
        String orderNote = json.get("orderNote").asText();
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new ResourceNotFoundException("User not found by name: " + userName));
        Shop shop = user.getCustomer().getShop();

        order.setCustomer(customerService.findCustomerById(tenantId));
        order.setShop(shop);
        order.setOrderNumber(orderNumber);
        order.setOrderNote(orderNote);
        order.setOrderItems(new ArrayList<>());
        order.setOrderDate(new Date());
        order.setCreatedBy(user);
        order.setInvoiceAddress(addressService.findAddressById(invoiceAddressId));
        order.setReceiverAddress(addressService.findAddressById(receiverAddressId));
        order.setOrderStatus(OrderStatus.OLUSTURULDU);

        List<BasketItem> basketItems = basketService.findBasketById(basketId).getBasketItems();
        Double totalPrice = 0.0;
        Double withoutTaxPrice = 0.0;
        Double totalTax = 0.0;

        for (BasketItem basketItem : basketItems) {
            Product refProduct = basketItem.getProduct();
            boolean isStockAvailable = productService.checkStockById(refProduct.getId(), basketItem.getQuantity());

            if (isStockAvailable) {
                List<Image> imagesCopy = new ArrayList<>();
                for (Image image : refProduct.getImages()) {
                    Image imageCopy = new Image();
                    imageCopy.setUrl(image.getUrl());
                    imageCopy.setIsThumbnail(image.getIsThumbnail());
                    imagesCopy.add(imageCopy);
                }

                OrderItem orderItem = OrderItem.builder()
                        .refProductId(refProduct.getId())
                        .name(refProduct.getName())
                        .salesPrice(refProduct.getSalesPrice())
                        .grossPrice(refProduct.getGrossPrice())
                        .quantity(basketItem.getQuantity())
                        .images(imagesCopy)
                        .build();

                totalPrice += refProduct.getGrossPrice() * basketItem.getQuantity();
                withoutTaxPrice += refProduct.getSalesPrice() * basketItem.getQuantity();
                totalTax = totalPrice - withoutTaxPrice;

                refProduct.setStock(refProduct.getStock() - basketItem.getQuantity());

                order.getOrderItems().add(orderItem);
            } else {
                throw new ResourceNotFoundException("Stock is not enough for material: " + refProduct.getName());
            }
        }

        order.setTotalPrice(totalPrice);
        order.setWithoutTaxPrice(withoutTaxPrice);
        order.setTotalTax(totalTax);

        basketRepository.deleteById(basketId);

        return orderRepository.save(order);
    }

    public static String generateOrderNumber(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        String timestamp = now.format(formatter);

        String orderNumber = tenantId.toString() + timestamp.substring(4, 11);

        return orderNumber;
    }

    public Order findOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Order could not find by id: " + id));
    }

    @Transactional
    public Order updateOrder(Long id, JsonNode json) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order could not find by id: " + id));

        int statusId = json.get("orderStatus").get("id").asInt();
        String statusText = json.get("orderStatus").get("status").asText();

        OrderStatus newStatusById = OrderStatus.getById(statusId);
        if (!newStatusById.getStatus().toUpperCase(Locale.ROOT).equals(statusText)) {
            throw new IllegalArgumentException("OrderStatus id and status do not match");
        }

        order.setOrderStatus(newStatusById);

        if (newStatusById == OrderStatus.IPTAL_EDILDI) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = productRepository.findById(item.getRefProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product could not find by id: " + item.getRefProductId()));
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        return orderRepository.save(order);
    }

//    public void deleteOrder(Long id) {
//        Order order = findOrderById(id);
//        orderRepository.delete(order);
//    }
}
