package com.example.demo.controller;

import com.example.demo.dto.QuoteItemRequestDTO;
import com.example.demo.dto.QuoteItemResponseDTO;
import com.example.demo.service.QuoteItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/quotes/{quoteId}/items")
public class QuoteItemController {
  private final QuoteItemService service;

  public QuoteItemController(QuoteItemService service) {
    this.service = service;
  }

  @PostMapping
  public QuoteItemResponseDTO add(
      @PathVariable UUID quoteId,
      @RequestBody QuoteItemRequestDTO dto) {
    dto.setQuoteId(quoteId);
    return service.addItem(dto);
  }

  @PutMapping("/{itemId}")
  public QuoteItemResponseDTO update(
      @PathVariable UUID itemId,
      @RequestBody QuoteItemRequestDTO dto) {
    return service.updateItem(itemId, dto);
  }

  @DeleteMapping("/{itemId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID itemId) {
    service.deleteItem(itemId);
    return ResponseEntity.noContent().build();
  }
}