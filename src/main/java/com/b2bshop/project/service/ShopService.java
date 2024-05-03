package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateShopRequest;
import com.b2bshop.project.exception.ShopNotFoundException;
import com.b2bshop.project.model.Shop;
import com.b2bshop.project.repository.ShopRepository;
import org.springframework.stereotype.Service;

@Service
public class ShopService {

    private final ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public Shop createShop(CreateShopRequest request) {
        Shop newShop = Shop.builder()
                .name(request.name())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .vatNumber(request.vatNumber())
                .aboutUs(request.aboutUs())
                .privacyPolicy(request.privacyPolicy())
                .build();

        return shopRepository.save(newShop);
    }

    public Shop updateShopById(Long shopId, Shop newShop) {
        Shop shop = findShopById(shopId);
        shop.setName(newShop.getName());
        shop.setEmail(newShop.getEmail());
        shop.setPhoneNumber(newShop.getPhoneNumber());
        shop.setVatNumber(newShop.getVatNumber());
        shop.setAboutUs(newShop.getAboutUs());
        shop.setPrivacyPolicy(newShop.getPrivacyPolicy());
        shopRepository.save(shop);
        return shop;
    }

    public Shop findShopById(Long id) {
        return shopRepository.findById(id).orElseThrow(()
                -> new ShopNotFoundException("Shop could not find by id: " + id));
    }
}
