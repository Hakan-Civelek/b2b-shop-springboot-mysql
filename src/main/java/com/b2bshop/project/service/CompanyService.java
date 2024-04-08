package com.b2bshop.project.service;

import com.b2bshop.project.dto.CreateCompanyRequest;
import com.b2bshop.project.model.Company;
import com.b2bshop.project.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Optional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @PostMapping
    public Company createCompany(CreateCompanyRequest request) {
        Company newCompany = Company.builder()
                .name(request.name())
                .email(request.email())
                .users(request.users())
                .build();

        return companyRepository.save(newCompany);
    }

    public Company updateCompanyById(Long companyId, Company newCompany) {
        Optional<Company> company = companyRepository.findById(companyId);
        if (company.isPresent()) {
            Company oldCompany = company.get();
            oldCompany.setName(newCompany.getName());
            oldCompany.setEmail(newCompany.getEmail());
            oldCompany.setUsers(newCompany.getUsers());
            companyRepository.save(oldCompany);
            return oldCompany;
        } else return null;
    }
}
