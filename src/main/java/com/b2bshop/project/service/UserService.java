package com.b2bshop.project.service;

import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.*;
import com.b2bshop.project.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomerService customerService;
    private final EntityManager entityManager;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtService jwtService, CustomerService customerService, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.customerService = customerService;
        this.entityManager = entityManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElseThrow(EntityNotFoundException::new);
    }

    public User getMe(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        Optional<User> user = userRepository.findByUsername(userName);
        Long userId = user.get().getId();

        return findUserById(userId);
    }

    public User createUser(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(()
                -> new ResourceNotFoundException("User not found by name: " + userName));
        Shop shop = user.getShop();
        Set<Role> authorities = new HashSet<>();
        authorities.add(Role.valueOf(json.get("authorities").asText()));

        User newUser = User.builder()
                .name(json.get("name").asText())
                .username(json.get("username").asText())
                .password(passwordEncoder.encode(json.get("password").asText()))
                .email(json.get("email").asText())
                .phoneNumber(json.get("phoneNumber").asText())
                .authorities(authorities)
                .isEnabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .isActive(true)
                .build();

        if (json.get("authorities").asText().equals("ROLE_CUSTOMER_USER")) {
            newUser.setCustomer(customerService.findCustomerById(json.get("customerTenantId").asLong()));
        } else {
            newUser.setShop(shop);
        }

        return userRepository.save(newUser);
    }

    public User updateUserById(Long userId, User newUser) {
        User oldUser = (userRepository.findById(userId).orElseThrow(()
                -> new RuntimeException("User not found")));
        oldUser.setName(newUser.getName());
        oldUser.setUsername(newUser.getUsername());
        oldUser.setPassword(passwordEncoder.encode(newUser.getPassword())); //TODO need an update for password changed!
        oldUser.setEmail(newUser.getEmail());
        oldUser.setPhoneNumber(newUser.getPhoneNumber());
        oldUser.setAuthorities(newUser.getAuthorities());
        oldUser.setShop(newUser.getShop());
        oldUser.setCustomer(newUser.getCustomer());
        oldUser.setActive(newUser.isActive());
        userRepository.save(oldUser);
        return oldUser;
    }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("User could not find by id: " + id));
    }

    public User findUserByName(String name) {
        return userRepository.findByUsername(name).orElseThrow(()
                -> new ResourceNotFoundException("User could not find by name: " + name));
    }

    public List<User> getAllUsers(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Set<Role> userRoles = user.getAuthorities();

        if (userRoles.contains(Role.ROLE_CUSTOMER_USER)) {
            return userRepository.findAllByCustomerTenantId(user.getCustomer().getTenantId());
        } else if (userRoles.contains(Role.ROLE_SHOP_OWNER)) {
            Long shopTenantId = user.getShop().getTenantId();
            return userRepository.findAllByCustomerShopTenantId(shopTenantId);
        } else if (userRoles.contains(Role.ROLE_SYSTEM_OWNER)) {
            return userRepository.findAll();
        } else {
            return List.of();
        }
    }

    public User findUserByUserName(String userName){
        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT user " +
                " FROM User as user " +
                " WHERE user.username = :userName ";

        Query<User> query = session.createQuery(hqlQuery, User.class);
        query.setParameter("userName", userName);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new EntityNotFoundException("User not found by name: " + userName);
        }
    }
}
