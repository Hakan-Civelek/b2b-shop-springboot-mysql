package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateShopRequest;
import com.b2bshop.project.model.Shop;
import com.b2bshop.project.repository.ShopRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

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
                .users(request.users())
                .companies(request.companies())
                .build();

        return shopRepository.save(newShop);
    }

    public Shop updateShopById(Long shopId, Shop newShop) {
        Optional<Shop> shop = shopRepository.findById(shopId);
        if (shop.isPresent()) {
            Shop oldShop = shop.get();
            oldShop.setName(newShop.getName());
            oldShop.setEmail(newShop.getEmail());
            oldShop.setUsers(newShop.getUsers());
            shopRepository.save(oldShop);
            return oldShop;
        } else return null;
    }
}
