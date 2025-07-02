package com.example.demo.service;

import com.example.demo.dto.CustomerDTO;
import com.example.demo.dto.CustomerInfoDTO;
import com.example.demo.entity.Company;
import com.example.demo.entity.Customer;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {
  private final CustomerRepository repository;
  private final CompanyRepository companyRepository;

  public CustomerService(CustomerRepository repository, CompanyRepository companyRepository) {
    this.repository = repository;
    this.companyRepository = companyRepository;
  }

  @Transactional
  public CustomerDTO createCustomer(CustomerInfoDTO info, UUID companyId) {
    // Hämta Company-entiteten
    Company company = companyRepository.findById(companyId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    // Skapa Customer med relation
    Customer entity = Customer.builder()
        .company(company)
        .name(info.getName())
        .address(info.getAddress())
        .phone(info.getPhone())
        .email(info.getEmail())
        .build();
    Customer saved = repository.save(entity);
    return toDTO(saved);
  }

  @Transactional(readOnly = true)
  public List<CustomerDTO> listCustomers(UUID companyId) {
    // findByCompanyId navigerar via company.id
    return repository.findByCompanyId(companyId).stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public CustomerDTO getCustomer(UUID id) {
    Customer c = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Customer not found"));
    return toDTO(c);
  }

  @Transactional
  public CustomerDTO updateCustomer(UUID id, CustomerInfoDTO info) {
    Customer entity = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Customer not found"));
    entity.setName(info.getName());
    entity.setAddress(info.getAddress());
    entity.setPhone(info.getPhone());
    entity.setEmail(info.getEmail());
    Customer updated = repository.save(entity);
    return toDTO(updated);
  }

  @Transactional
  public void deleteCustomer(UUID id) {
    repository.deleteById(id);
  }

  private CustomerDTO toDTO(Customer c) {
    CustomerDTO dto = new CustomerDTO();
    dto.setId(c.getId());
    // Använd relationen för att hämta company-id
    dto.setCompanyId(c.getCompany().getId());
    dto.setName(c.getName());
    dto.setAddress(c.getAddress());
    dto.setPhone(c.getPhone());
    dto.setEmail(c.getEmail());
    dto.setCreatedAt(c.getCreatedAt());
    return dto;
  }
}
