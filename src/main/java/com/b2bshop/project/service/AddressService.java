package com.b2bshop.project.service;

import com.b2bshop.project.exception.CustomerNotFoundException;
import com.b2bshop.project.model.Address;
import com.b2bshop.project.model.Country;
import com.b2bshop.project.model.Customer;
import com.b2bshop.project.repository.AddressRepository;
import com.b2bshop.project.repository.CountryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AddressService {
    private final CountryRepository countryRepository;
    private final SecurityService securityService;
    private final CustomerService customerService;
    private final EntityManager entityManager;
    private final AddressRepository addressRepository;

    public AddressService(CountryRepository countryRepository, SecurityService securityService,
                          CustomerService customerService, EntityManager entityManager, AddressRepository addressRepository) {
        this.countryRepository = countryRepository;
        this.securityService = securityService;
        this.customerService = customerService;
        this.entityManager = entityManager;
        this.addressRepository = addressRepository;
    }

    public List<Map<String, Object>> getAllAddresses(HttpServletRequest request) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);
        Session session = entityManager.unwrap(Session.class);

        String hqlQuery = "SELECT address.id as id, country.name as countryName, address.title as title," +
                " address.city as city, address.addressLine as addressLine " +
                " FROM Address as address " +
                " JOIN address.customer as customer " +
                " JOIN address.country as country " +
                " WHERE customer.id = :tenantId";

        Query query = session.createQuery(hqlQuery);
        query.setParameter("tenantId", tenantId);

        List<Object[]> results = query.list();

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> addressMap = new HashMap<>();
            addressMap.put("id", result[0]);
            addressMap.put("countryName", result[1]);
            addressMap.put("title", result[2].toString());
            addressMap.put("city", result[3].toString());
            addressMap.put("addressLine", result[4].toString());
            resultList.add(addressMap);
        }

        return resultList;
    }

    @Transactional
    public Address createAddress(HttpServletRequest request, JsonNode json) {
        String token = request.getHeader("Authorization").split("Bearer ")[1];
        Long tenantId = securityService.returnTenantIdByUsernameOrToken("token", token);
        Long countryId = json.get("countryId").asLong();

        Address address = new Address();

        Customer customer;
        customer = customerService.findCustomerById(tenantId);
        address.setCustomer(customer);

        Country country;
        country = countryRepository.findById(countryId).orElse(null);
        address.setCountry(country);

        address.setTitle(json.get("title").asText());
        address.setCity(json.get("city").asText());
        address.setAddressLine(json.get("addressLine").asText());

        return addressRepository.save(address);
    }

    protected Address findAddressById(Long id) {
        return addressRepository.findById(id).orElseThrow(
                () -> new CustomerNotFoundException("Address could not find by id: " + id));
    }
}
