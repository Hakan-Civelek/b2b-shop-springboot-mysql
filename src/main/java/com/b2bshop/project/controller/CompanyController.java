package com.b2bshop.project.controller;

import com.b2bshop.project.dto.CreateCompanyRequest;
import com.b2bshop.project.model.Company;
import com.b2bshop.project.repository.CompanyRepository;
import com.b2bshop.project.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/company")
@Slf4j
public class CompanyController {
    private final CompanyService companyService;
    private final CompanyRepository companyRepository;

    public CompanyController(CompanyService companyService, CompanyRepository companyRepository) {
        this.companyService = companyService;
        this.companyRepository = companyRepository;
    }

    @GetMapping()
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @PostMapping()
    public List<Company> addCompany(@RequestBody List<CreateCompanyRequest> requests) {
        List<Company> createdCompanies = new ArrayList<>();
        for (CreateCompanyRequest request : requests) {
            companyService.createCompany(request);
        }
        return createdCompanies;
    }

    public Company addCompany(@RequestBody CreateCompanyRequest request) {
        return companyService.createCompany(request);
    }

    @GetMapping("/{companyId}")
    public Company getCompanyById(@PathVariable Long companyId) {
        return companyRepository.findById(companyId).orElse(null);
    }

    @PutMapping("/{companyId}")
    public Company updateCompanyById(@PathVariable Long companyId, @RequestBody Company newCompany) {
        return companyService.updateCompanyById(companyId, newCompany);
    }

    @DeleteMapping("/{companyId}")
    public void deleteCompanyById(@PathVariable Long companyId) {
        companyRepository.deleteById(companyId);
    }
}
