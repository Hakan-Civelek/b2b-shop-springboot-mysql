package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateCustomerRequest;
import com.b2bshop.project.exception.CustomerNotFoundException;
import com.b2bshop.project.model.Customer;
import com.b2bshop.project.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;


@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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
                -> new CustomerNotFoundException("Customer could not find by id: " + id));
    }
}
