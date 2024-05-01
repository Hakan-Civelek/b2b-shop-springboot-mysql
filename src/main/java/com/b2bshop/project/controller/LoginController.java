package com.b2bshop.project.controller;

import com.b2bshop.project.dto.AuthRequest;
import com.b2bshop.project.model.Customer;
import com.b2bshop.project.model.Role;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.CustomerRepository;
import com.b2bshop.project.repository.UserRepository;
import com.b2bshop.project.service.JwtService;
import com.b2bshop.project.service.SecurityService;
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
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SecurityService securityService;

    public LoginController(JwtService jwtService, AuthenticationManager authenticationManager, SecurityService securityService,
                           UserRepository userRepository,
                           CustomerRepository customerRepository) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.securityService = securityService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    @PostMapping()
    public Map generateToken(@RequestBody AuthRequest request) {
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("userName", request.username());
        User user = (userRepository.findByUsername(request.username()).orElseThrow(()
                -> new RuntimeException("User not found")));
        Set userRoles = user.getAuthorities();
        if (!(request.username().equals("hakan") || request.username().equals("esat"))) {
            Customer customer = (customerRepository.findById(tenantId).orElseThrow(()
                    -> new RuntimeException("Customer not found")));
            if (userRoles.contains(Role.ROLE_CUSTOMER_USER) && !customer.isActive()) {
                throw new RuntimeException("Customer not active!");
            }
        }

        //TODO check the above one! Maybe you'll fix the problem with the controllers
        if (tenantId == request.tenantId() || request.username().equals("hakan") || request.username().equals("esat")) {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            if (authentication.isAuthenticated()) {
                return jwtService.generateToken(request.username());
            }
        }
        log.info("invalid username " + request.username());
        throw new UsernameNotFoundException("invalid username {} " + request.username());
    }
}
