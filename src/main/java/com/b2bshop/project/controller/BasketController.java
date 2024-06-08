package com.b2bshop.project.controller;

import com.b2bshop.project.model.Basket;
import com.b2bshop.project.repository.BasketRepository;
import com.b2bshop.project.service.BasketService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/basket")
public class BasketController {
    private final BasketRepository basketRepository;
    private final BasketService basketService;

    public BasketController(BasketRepository basketRepository, BasketService basketService) {
        this.basketRepository = basketRepository;
        this.basketService = basketService;
    }

    @GetMapping()
    public Map<String, Object> getBasket(HttpServletRequest request) {
        return basketService.getBasket(request);
    }

    @PostMapping("/addItem")
    public Map<String, String> addItemOnBasket(HttpServletRequest request, @RequestBody JsonNode json) {
        return basketService.addItemOnBasket(request, json);
    }

    @PostMapping("/removeItem")
    public Basket removeItemOnBasket(HttpServletRequest request, @RequestBody JsonNode json) {
        return basketService.removeItem(request, json);
    }

    @GetMapping("/{basketId}")
    public Basket getBasketById(@PathVariable Long basketId) {
        return basketService.findBasketById(basketId);
    }

    @PostMapping("/cleanBasket")
    public Map<String, String> cleanBasket(HttpServletRequest request) {
        return basketService.cleanBasket(request);
    }

    @DeleteMapping("/{basketId}")
    public void deleteBasketById(@PathVariable Long basketId) {
        basketRepository.deleteById(basketId);
    }
}
