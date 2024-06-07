package com.b2bshop.project.service;

import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.Customer;
import com.b2bshop.project.model.Role;
import com.b2bshop.project.model.Shop;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.CustomerRepository;
import com.b2bshop.project.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, JwtService jwtService, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Customer> getAllCustomers(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("User not found"));
        Shop shop = user.getShop();
        return customerRepository.findAllByShop(shop);
    }

    @Transactional
    public Customer createCustomer(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User shopUser = userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("User not found"));
        Shop shop = shopUser.getShop();

        Customer customer = new Customer();
        customer.setName(json.get("name").asText());
        customer.setEmail(json.get("email").asText());
        customer.setShop(shop);
        customer.setVatNumber(json.get("vatNumber").asText());
        customer.setPhoneNumber(json.get("phoneNumber").asText());
        customer.setActive(true);

        //Create default user!
        User customerUser = new User();
        customerUser.setUsername(json.get("email").asText());
        customerUser.setEmail(json.get("email").asText());
        customerUser.setPhoneNumber(json.get("phoneNumber").asText());
        customerUser.setPassword(passwordEncoder.encode("password"));
        customerUser.setCustomer(customer);
        customerUser.setAuthorities(Set.of(Role.ROLE_CUSTOMER_USER));
        customerUser.setActive(true);
        customerUser.setEnabled(true);
        customerUser.setAccountNonExpired(true);
        customerUser.setAccountNonLocked(true);
        customerUser.setCredentialsNonExpired(true);
        userRepository.save(customerUser);

        return customerRepository.save(customer);
    }

    public Customer updateCustomerById(Long customerId, Customer newCustomer) {
        Customer customer = findCustomerById(customerId);
        customer.setName(newCustomer.getName());
        customer.setEmail(newCustomer.getEmail());
        customer.setShop(newCustomer.getShop());
        customer.setVatNumber(newCustomer.getVatNumber());
        customer.setPhoneNumber(newCustomer.getPhoneNumber());
        customer.setActive(newCustomer.isActive());
        customerRepository.save(customer);
        return customer;
    }

    public Customer findCustomerById(Long id) {
        return customerRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Customer could not find by id: " + id));
    }
}
