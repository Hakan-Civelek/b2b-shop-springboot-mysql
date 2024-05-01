package com.b2bshop.project.security;

import com.b2bshop.project.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;


    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserService userService, PasswordEncoder passwordEncoder) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(x -> x
                        .requestMatchers("/api/**").permitAll()
//                        .requestMatchers("/api/user/createSystemOwners").permitAll()
//                        .requestMatchers("/api/login").permitAll()
//                        .requestMatchers(HttpMethod.PUT, "/api/shop/**").hasRole("SHOP_OWNER")
//                        .requestMatchers(HttpMethod.GET, "/api/shop/**").hasRole("SHOP_OWNER")
//                        .requestMatchers(HttpMethod.GET, "/api/shop/**").hasRole("SHOP_USER")
//                        .requestMatchers("/api/customer/**").hasRole("SHOP_OWNER")
//                        .requestMatchers(HttpMethod.GET,"/api/customer/**").hasRole("SHOP_USER")
//                        .requestMatchers("/api/product/**").hasRole("SHOP_OWNER")
//                        .requestMatchers(HttpMethod.GET,"/api/product/**").hasRole("SHOP_USER")
//                        .requestMatchers("/api/user/**").hasRole("SHOP_OWNER")
//                        .requestMatchers(HttpMethod.GET,"/api/user/**").hasRole("SHOP_USER")
//                        .requestMatchers(HttpMethod.GET,"/api/user/**").hasRole("SHOP_USER")
//                        .requestMatchers("/api/**").hasRole("SYSTEM_OWNER")
                        .anyRequest().authenticated()
                )
                .sessionManagement(x -> x
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();

    }
}
