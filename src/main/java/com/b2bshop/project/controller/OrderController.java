package com.b2bshop.project.controller;

import com.b2bshop.project.model.Order;
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

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping()
    public List<Map<String, Object>> getAllOrders(HttpServletRequest request) {
        return orderService.getAllOrders(request);
    }

    @PostMapping()
    public Order addOrder(HttpServletRequest request, @RequestBody JsonNode json) {
        return orderService.createOrder(request, json);
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.findOrderById(id);
    }

    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable Long id, @RequestBody JsonNode json) {
        return orderService.updateOrder(id, json);
    }

//    @DeleteMapping("/{id}")
//    public void deleteOrder(@PathVariable Long id) {
//        orderService.deleteOrder(id);
//    }
}