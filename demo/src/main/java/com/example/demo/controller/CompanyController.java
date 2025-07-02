package com.example.demo.controller;

import com.example.demo.dto.CompanyDTO;
import com.example.demo.dto.CompanyInfoDTO;
import com.example.demo.service.CompanyService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
  private final CompanyService service;

  public CompanyController(CompanyService service) {
    this.service = service;
  }

  // GET /api/companies/me
  @GetMapping("/me")
  public ResponseEntity<CompanyDTO> getMe(Principal principal) {
    String email = principal.getName();   // nu e-post, inte UUID

    return service.findByEmail(email)     // Optional<Company>
        .map(service::toDTO)              // CompanyDTO
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/me/logo")
  public ResponseEntity<Void> uploadLogo(@RequestBody Map<String, String> payload) {
    String base64 = payload.get("logoBase64");
    UUID companyId = service.getCurrentCompanyId();
    service.updateLogo(companyId, base64);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/me/logo")
  public ResponseEntity<Map<String, String>> getLogo() {
    UUID companyId = service.getCurrentCompanyId();
    String base64 = service.getLogoBase64(companyId);
    return ResponseEntity.ok(Map.of("logoBase64", base64));
  }

  // POST /api/companies
  @PostMapping
  public ResponseEntity<CompanyDTO> create(@RequestBody CompanyInfoDTO info) {
    CompanyDTO created = service.createCompany(info);
    return ResponseEntity.ok(created);
  }

  // PUT /api/companies/{id}
  @PutMapping("/{id}")
  public ResponseEntity<CompanyDTO> update(
      @PathVariable UUID id,
      @RequestBody CompanyInfoDTO info
  ) {
    CompanyDTO updated = service.updateCompany(id, info);
    return ResponseEntity.ok(updated);
  }
}