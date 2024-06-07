package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateShopRequest;
import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.Role;
import com.b2bshop.project.model.Shop;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.ShopRepository;
import com.b2bshop.project.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ShopService(ShopRepository shopRepository,
                       UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Shop createShop(CreateShopRequest request) {
        Shop shop = Shop.builder()
                .name(request.name())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .vatNumber(request.vatNumber())
                .aboutUs(request.aboutUs())
                .privacyPolicy(request.privacyPolicy())
                .build();

        //Create default user!
        User user = new User();
        user.setUsername(request.email());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode("password"));
        user.setShop(shop);
        user.setAuthorities(Set.of(Role.ROLE_SHOP_OWNER));
        user.setActive(true);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        userRepository.save(user);

        return shopRepository.save(shop);
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
                -> new ResourceNotFoundException("Shop could not find by id: " + id));
    }
}
