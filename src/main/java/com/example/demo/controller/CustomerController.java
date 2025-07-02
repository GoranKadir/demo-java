package com.example.demo.controller;

import com.example.demo.dto.CustomerDTO;
import com.example.demo.dto.CustomerInfoDTO;
import com.example.demo.service.CompanyService;
import com.example.demo.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.CompanyDTO;


import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
  private final CustomerService service;

  private final CompanyService companyService;

  public CustomerController(CustomerService service, CompanyService companyService) {
    this.service = service;
    this.companyService = companyService;
  }

  // GET /api/customers
  @GetMapping
  public ResponseEntity<List<CustomerDTO>> list(Principal principal) {
    UUID companyId = companyService
        .findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    List<CustomerDTO> customers = service.listCustomers(companyId);
    return ResponseEntity.ok(customers);
  }

  // POST /api/customers
  @PostMapping
  public ResponseEntity<CustomerDTO> create(
      Principal principal,
      @RequestBody CustomerInfoDTO info
  ) {
    UUID companyId = companyService
        .findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    info.setCompanyId(companyId);
    CustomerDTO created = service.createCustomer(info, companyId);
    return ResponseEntity.ok(created);
  }

  // GET /api/customers/{id}
  @GetMapping("/{id}")
  public ResponseEntity<CustomerDTO> get(@PathVariable UUID id, Principal principal) {
    UUID companyId = companyService
        .findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    CustomerDTO dto = service.getCustomer(id);

    if (!dto.getCompanyId().equals(companyId)) {
      return ResponseEntity.status(403).build(); // eller 404 för att dölja existensen
    }

    return ResponseEntity.ok(dto);
  }


  // PUT /api/customers/{id}
  @PutMapping("/{id}")
  public ResponseEntity<CustomerDTO> update(
      @PathVariable UUID id,
      @RequestBody CustomerInfoDTO info
  ) {
    CustomerDTO updated = service.updateCustomer(id, info);
    return ResponseEntity.ok(updated);
  }

  // DELETE /api/customers/{id}
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.deleteCustomer(id);
    return ResponseEntity.noContent().build();
  }
}
