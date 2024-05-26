package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateCustomerRequest;
import com.b2bshop.project.exception.ResourceNotFoundException;
import com.b2bshop.project.model.Customer;
import com.b2bshop.project.model.Shop;
import com.b2bshop.project.model.User;
import com.b2bshop.project.repository.CustomerRepository;
import com.b2bshop.project.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;


@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public CustomerService(CustomerRepository customerRepository, JwtService jwtService, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public List<Customer> getAllCustomers(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        String userName = jwtService.extractUser(token);
        User user = userRepository.findByUsername(userName).orElseThrow(() -> new RuntimeException("User not found"));
        Shop shop = user.getShop();
        return customerRepository.findAllByShop(shop);
    }

    @PostMapping
    public Customer createCustomer(CreateCustomerRequest request) {
        Customer newCustomer = Customer.builder()
                .name(request.name())
                .email(request.email())
                .shop(request.shop())
                .vatNumber(request.vatNumber())
                .phoneNumber(request.phoneNumber())
                .isActive(request.isActive())
                .build();

        return customerRepository.save(newCustomer);
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
