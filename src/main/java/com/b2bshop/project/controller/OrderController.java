package com.b2bshop.project.controller;

import com.b2bshop.project.model.Order;
import com.b2bshop.project.repository.OrderRepository;
import com.b2bshop.project.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @GetMapping()
    public List<Map<String, Object>> getAllOrders(HttpServletRequest request) {
        return orderService.getAllOrders(request);
    }

    @PostMapping()
    public Order addOrder(HttpServletRequest request, @RequestBody JsonNode json) {
        return orderService.createOrder(request,json);
    }
}
