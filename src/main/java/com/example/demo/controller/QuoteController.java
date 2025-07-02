// src/main/java/com/example/demo/controller/QuoteController.java
package com.example.demo.controller;

import com.example.demo.dto.CompanyDTO;
import com.example.demo.dto.QuoteRequestDTO;
import com.example.demo.dto.QuoteResponseDTO;
import com.example.demo.service.CompanyService;
import com.example.demo.service.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {
  private final QuoteService quoteService;
  private final CompanyService companyService;

  public QuoteController(QuoteService quoteService, CompanyService companyService) {
    this.quoteService = quoteService;
    this.companyService = companyService;
  }

  @PostMapping("/draft")
  public ResponseEntity<QuoteResponseDTO> createDraft(Principal principal) {
    UUID companyId = companyService.findDtoByEmail(principal.getName())
        .map(dto -> dto.getId())
        .orElseThrow(() -> new RuntimeException("Company not found"));

    QuoteResponseDTO draft = quoteService.createDraft(companyId);
    return ResponseEntity.ok(draft);
  }

  @GetMapping
  public List<QuoteResponseDTO> list(Principal principal) {
    // principal.getName() är e-post
    UUID companyId = companyService
        .findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));


    return quoteService.listQuotes(companyId);
  }

  @PostMapping
  public QuoteResponseDTO createOrUpdate(
      Principal principal,
      @RequestBody QuoteRequestDTO dto
  ) {
    UUID companyId = companyService.findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    return quoteService.createOrUpdateQuote(dto, companyId);
  }

  @PutMapping("/{quoteId}")
  public QuoteResponseDTO update(
      Principal principal,
      @PathVariable UUID quoteId,
      @RequestBody QuoteRequestDTO dto
  ) {
    UUID companyId = companyService.findDtoByEmail(principal.getName())
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    dto.setId(quoteId);
    return quoteService.createOrUpdateQuote(dto, companyId);
  }

  @GetMapping("/{quoteId}")
  public QuoteResponseDTO get(
      Principal principal,
      @PathVariable UUID quoteId
  ) {
    // kontrollera company som ägare, ev. 404 om inte hittas
    return quoteService.getQuote(quoteId, principal.getName());
  }
  @DeleteMapping("/draft/{quoteId}")
  public ResponseEntity<Void> deleteDraft(
      Principal principal,
      @PathVariable UUID quoteId
  ) {
    // validera att quoteId hör till inloggat företag…
    quoteService.deleteDraft(quoteId, principal.getName());
    return ResponseEntity.noContent().build();
  }
}
