package com.b2bshop.project.controller;

import com.b2bshop.project.dto.CreateCustomerRequest;
import com.b2bshop.project.model.Customer;
import com.b2bshop.project.repository.CustomerRepository;
import com.b2bshop.project.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    public CustomerController(CustomerService customerService, CustomerRepository customerRepository) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    @GetMapping()
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @PostMapping()
    public List<Customer> addCustomer(@RequestBody List<CreateCustomerRequest> requests) {
        List<Customer> createdCustomers = new ArrayList<>();
        for (CreateCustomerRequest request : requests) {
            customerService.createCustomer(request);
        }
        return createdCustomers;
    }

    public Customer addCustomer(@RequestBody CreateCustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @GetMapping("/{customerId}")
    public Customer getCustomerById(@PathVariable Long customerId) {
        return customerRepository.findById(customerId).orElse(null);
    }

    @PutMapping("/{customerId}")
    public Customer updateCustomerById(@PathVariable Long customerId, @RequestBody Customer newCustomer) {
        return customerService.updateCustomerById(customerId, newCustomer);
    }

    @DeleteMapping("/{customerId}")
    public void deleteCustomerById(@PathVariable Long customerId) {
        customerRepository.deleteById(customerId);
    }
}
