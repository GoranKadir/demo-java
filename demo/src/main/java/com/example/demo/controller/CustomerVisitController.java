package com.example.demo.controller;

import com.example.demo.dto.CompanyDTO;
import com.example.demo.dto.CustomerVisitDTO;

import com.example.demo.dto.VisitAreaDTO;
import com.example.demo.entity.Company;
import com.example.demo.entity.CustomerVisit;

import com.example.demo.service.CompanyService;
import com.example.demo.service.CustomerVisitService;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/customer-visits")
public class CustomerVisitController {

  private final CustomerVisitService visitService;
  private final CompanyService companyService;

  public CustomerVisitController(CustomerVisitService visitService, CompanyService companyService) {
    this.visitService = visitService;
    this.companyService = companyService;
  }

  @PostMapping
  public ResponseEntity<?> createVisit(@RequestBody CustomerVisitDTO dto, Principal principal) {
    UUID companyId = companyService
        .findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    CustomerVisit saved = visitService.createVisit(dto, companyId);
    return ResponseEntity.created(URI.create("/api/customer-visits/" + saved.getId())).body(saved.getId());
  }
  @PutMapping("/{id}")
  public ResponseEntity<Void> updateCustomerVisit(
      @PathVariable UUID id,
      @RequestBody CustomerVisitDTO dto,
      Principal principal
  ) {
    UUID companyId = companyService
        .findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    visitService.update(id, dto, companyId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/draft")
  public ResponseEntity<String> createDraftVisit(Principal principal) {
    UUID companyId = companyService
        .findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    CustomerVisit saved = visitService.createDraftVisit(companyId);
    return ResponseEntity.ok(saved.getId().toString());
  }

  @GetMapping
  public ResponseEntity<List<CustomerVisitDTO>> listVisits(Principal principal) {
    UUID companyId = companyService
        .findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    return ResponseEntity.ok(visitService.getVisitsForCompany(companyId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerVisitDTO> getVisit(@PathVariable UUID id, Principal principal) {
    UUID companyId = companyService
        .findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    return ResponseEntity.ok(visitService.getVisitById(id, companyId));
  }


}