package com.b2bshop.project.controller;

import com.b2bshop.project.model.Basket;
import com.b2bshop.project.repository.BasketRepository;
import com.b2bshop.project.service.BasketService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public List<Map<String, Object>> getAllBaskets(HttpServletRequest request) {
//        return basketRepository.findAll();
        return basketService.getAllBaskets(request);
    }

    @PostMapping()
    public List<Basket> addBasket(HttpServletRequest request, @RequestBody JsonNode json) {
//        List<Basket> createdBaskets = new ArrayList<>();
//        for (CreateBasketRequest basket : json) {
//            createdBaskets.add(basketService.createBasket(request, json));
            basketService.createBasket(request, json);
//        }
        return null;
    }

    @GetMapping("/{basketId}")
    public Basket getBasketById(@PathVariable Long basketId) {
        return basketRepository.findById(basketId).orElse(null);
    }

    @PutMapping("/{basketId}")
    public Basket updateBasketById(@PathVariable Long basketId, @RequestBody Basket newBasket) {
        return basketService.updateBasketById(basketId, newBasket);
    }

    @DeleteMapping("/{basketId}")
    public void deleteBasketById(@PathVariable Long basketId) {
        basketRepository.deleteById(basketId);
    }
}
