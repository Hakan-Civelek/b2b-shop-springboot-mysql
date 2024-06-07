package com.b2bshop.project.controller;

import com.b2bshop.project.dto.AuthRequest;
import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.Customer;
import com.b2bshop.project.model.Role;
import com.b2bshop.project.model.User;
import com.b2bshop.project.service.CustomerService;
import com.b2bshop.project.service.JwtService;
import com.b2bshop.project.service.SecurityService;
import com.b2bshop.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/login")
@Slf4j
public class LoginController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SecurityService securityService;
    private final CustomerService customerService;
    private final UserService userService;

    public LoginController(JwtService jwtService, AuthenticationManager authenticationManager, SecurityService securityService,
                           CustomerService customerService, UserService userService) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.securityService = securityService;
        this.customerService = customerService;
        this.userService = userService;
    }

    @PostMapping()
    public Map generateToken(@RequestBody AuthRequest request) {
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("userName", request.username());
        User user = (userService.findUserByUserName(request.username()));

        Set userRoles = user.getAuthorities();
        if (!(request.username().equals("hakan") || request.username().equals("esat")) && userRoles.contains(Role.ROLE_CUSTOMER_USER)) {
            Customer customer = customerService.findCustomerById(tenantId);
            if (!customer.isActive()) {
                throw new ResourceNotFoundException("Customer not active!");
            }
        }

        if (!user.isActive())
            throw new ResourceNotFoundException("User not active!");

        if (tenantId == request.tenantId() || request.username().equals("hakan") || request.username().equals("esat")) {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            if (authentication.isAuthenticated()) {
                return jwtService.generateToken(request.username());
            }
        }
        log.info("Invalid username or tenantId!");
        throw new ResourceNotFoundException("Invalid username or tenantId! ");
    }
}
