package com.b2bshop.project.service;

import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.*;
import com.b2bshop.project.repository.BrandRepository;
import com.b2bshop.project.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final SecurityService securityService;

    public BrandService(BrandRepository brandRepository, JwtService jwtService, UserRepository userRepository, EntityManager entityManager, SecurityService securityService) {
        this.brandRepository = brandRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.securityService = securityService;
    }

    public List<Map<String, Object>> getAllBrands(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);
        String whereCondition = " ";
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("User not found"));
        Set<Role> userRoles = user.getAuthorities();
        if (userRoles.contains(Role.ROLE_CUSTOMER_USER)) {
            whereCondition = " AND product.isActive = true ";
            tenantId = user.getCustomer().getShop().getTenantId();
        }

        Session session = entityManager.unwrap(Session.class);
        String hqlQuery = "SELECT brand.id brandId, brand.name brandName" +
                " FROM Brand as brand " +
                " JOIN brand.shop as shop " +
                " WHERE 1 = 1 ";

        if (tenantId != null) {
            hqlQuery += " AND shop.tenantId = :tenantId";
        }
        hqlQuery += whereCondition;

        Query query = session.createQuery(hqlQuery);

        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Object[]> rows = query.list();

        for (Object[] row : rows) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", row[0]);
            resultMap.put("name", row[1]);
            resultList.add(resultMap);
        }
        return resultList;
    }

    public Brand createBrand(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Shop shop = user.getShop();

        Brand brand = new Brand();
        brand.setName(json.get("name").asText());
        brand.setShop(shop);

        return brandRepository.save(brand);
    }

    public Brand updateBrand(Long id, JsonNode json) {
        Brand brand = findById(id);
        brand.setName((json.get("name").asText()));
        return brandRepository.save(brand);
    }

    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }

    public Brand findById(Long id) {
        return brandRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Brand could not find by id: " + id));
    }
}
