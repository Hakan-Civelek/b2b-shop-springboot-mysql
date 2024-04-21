package com.b2bshop.project.service;

import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public SecurityService(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public Long returnTenantIdByUsernameOrToken(String strType, String strValue) {
        Long response = null;
        if (strType.equals("userName")) {
            response = returnTenantIdByUsername(strValue);
        } else if (strType.equals("token")) {
            String userName = jwtService.extractUser(strValue);
            response = returnTenantIdByUsername(userName);
        }
        return response;
    }

    public Long returnTenantIdByUsername(String username) {
        Long response = null;
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            response = (user.getShop() != null) ? user.getShop().getTenantId() :
                    (user.getCustomer() != null) ? user.getCustomer().getTenantId() :
                            null;
        }
        return response;
    }
}
