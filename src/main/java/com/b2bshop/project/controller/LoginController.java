package com.b2bshop.project.controller;

import com.b2bshop.project.dto.AuthRequest;
import com.b2bshop.project.service.JwtService;
import com.b2bshop.project.service.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/login")
@Slf4j
public class LoginController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SecurityService securityService;

    public LoginController(JwtService jwtService, AuthenticationManager authenticationManager, SecurityService securityService) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.securityService = securityService;
    }

    @PostMapping()
    public Map generateToken(@RequestBody AuthRequest request) {
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("userName", request.username());
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
