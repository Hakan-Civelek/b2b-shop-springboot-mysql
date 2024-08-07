package com.b2bshop.project.controller;

import com.b2bshop.project.dto.CreateShopRequest;
import com.b2bshop.project.model.Shop;
import com.b2bshop.project.repository.ShopRepository;
import com.b2bshop.project.service.ShopService;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/shop")
public class ShopController {
    private final ShopService shopService;
    private final ShopRepository shopRepository;

    public ShopController(ShopService shopService, ShopRepository shopRepository) {
        this.shopService = shopService;
        this.shopRepository = shopRepository;
    }

    @GetMapping()
    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }

//    @PostMapping()
//    public List<Shop> addShop(@RequestBody List<CreateShopRequest> requests) {
//        List<Shop> createdShops = new ArrayList<>();
//        for (CreateShopRequest shop : requests) {
//            createdShops.add(shopService.createShop(shop));
//        }
//        return createdShops;
//    }

    @Transactional
    @PostMapping()
    public Shop addShop(@RequestBody CreateShopRequest request) {
        return shopService.createShop(request);
    }

    @GetMapping("/{shopId}")
    public Shop getShopById(@PathVariable Long shopId) {
        return shopService.findShopById(shopId);
    }

    @PutMapping("/{shopId}")
    public Shop updateShopById(@PathVariable Long shopId, @RequestBody Shop newShop) {
        return shopService.updateShopById(shopId, newShop);
    }

    @DeleteMapping("/{shopId}")
    public void deleteShopById(@PathVariable Long shopId) {
        shopRepository.deleteById(shopId);
    }
}
