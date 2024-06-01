package com.b2bshop.project.controller;

import com.b2bshop.project.model.Customer;
import com.b2bshop.project.repository.CustomerRepository;
import com.b2bshop.project.service.CustomerService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

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
    public List<Customer> getAllCustomers(HttpServletRequest request) {
        return customerService.getAllCustomers(request);
    }

//    @PostMapping()
//    public List<Customer> addCustomer(@RequestBody List<CreateCustomerRequest> requests) {
//        List<Customer> createdCustomers = new ArrayList<>();
//        for (CreateCustomerRequest request : requests) {
//            createdCustomers.add(customerService.createCustomer(request));
//        }
//        return createdCustomers;
//    }

    @PostMapping()
    public Customer addCustomer(HttpServletRequest request, @RequestBody JsonNode json) {
        return customerService.createCustomer(request, json);
    }

    @GetMapping("/{customerId}")
    public Customer getCustomerById(@PathVariable Long customerId) {
        return customerService.findCustomerById(customerId);
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
