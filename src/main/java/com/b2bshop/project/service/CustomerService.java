package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateCustomerRequest;
import com.b2bshop.project.model.Customer;
import com.b2bshop.project.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

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
                .build();

        return customerRepository.save(newCustomer);
    }

    public Customer updateCustomerById(Long customerId, Customer newCustomer) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isPresent()) {
            Customer oldCustomer = customer.get();
            oldCustomer.setName(newCustomer.getName());
            oldCustomer.setEmail(newCustomer.getEmail());
            oldCustomer.setShop(newCustomer.getShop());
            oldCustomer.setVatNumber(newCustomer.getVatNumber());
            oldCustomer.setPhoneNumber(newCustomer.getPhoneNumber());
            customerRepository.save(oldCustomer);
            return oldCustomer;
        } else return null;
    }
}
