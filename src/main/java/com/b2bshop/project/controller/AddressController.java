package com.b2bshop.project.controller;

import com.b2bshop.project.model.Address;
import com.b2bshop.project.service.AddressService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping()
    public List<Map<String, Object>> getAllProducts(HttpServletRequest request) {
        return addressService.getAllAddresses(request);
    }

    @PostMapping()
    public Address createAddress(HttpServletRequest request, @RequestBody JsonNode json) {
        return addressService.createAddress(request, json);
    }

}
