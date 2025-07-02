package com.example.demo.service;

import com.example.demo.dto.QuoteItemRequestDTO;
import com.example.demo.dto.QuoteItemResponseDTO;
import com.example.demo.entity.Quote;
import com.example.demo.entity.QuoteItem;
import com.example.demo.repository.QuoteItemRepository;
import com.example.demo.repository.QuoteRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class QuoteItemService {

  private final QuoteItemRepository repo;
  private final QuoteRepository quoteRepo;

  private final CompanyService companyService;

  public QuoteItemService(QuoteItemRepository repo, QuoteRepository quoteRepo,
      CompanyService companyService) {
    this.repo = repo;
    this.quoteRepo = quoteRepo;
    this.companyService = companyService;
  }

  @Transactional
  public QuoteItemResponseDTO addItem(QuoteItemRequestDTO dto) {
    Quote quote = quoteRepo.findById(dto.getQuoteId())
        .orElseThrow(() -> new RuntimeException("Quote not found"));

    // Säkerställ att alla fält är icke-null
    int quantity = defaultInt(dto.getQuantity(), 1);
    int discount = defaultInt(dto.getDiscount(), 0);
    int vat = defaultInt(dto.getVat(), 0);
    String type = defaultString(dto.getType(), "Material");
    String unit = defaultString(dto.getUnit(), "st");
    BigDecimal unitPrice = dto.getUnitPrice();

    QuoteItem item = QuoteItem.builder()
        .quote(quote)
        .description(dto.getDescription())
        .quantity(quantity)
        .unitPrice(unitPrice)
        .type(type)
        .unit(unit)
        .discount(discount)
        .vat(vat)
        .total(calculateTotal(quantity, discount, vat, unitPrice))
        .build();

    QuoteItem saved = repo.save(item);
    return toDTO(saved);
  }

  @Transactional
  public QuoteItemResponseDTO updateItem(UUID id, QuoteItemRequestDTO dto) {
    QuoteItem item = repo.findById(id)
        .orElseThrow(() -> new RuntimeException("Item not found"));

    int quantity = defaultInt(dto.getQuantity(), 1);
    int discount = defaultInt(dto.getDiscount(), 0);
    int vat = defaultInt(dto.getVat(), 0);
    String type = defaultString(dto.getType(), "Material");
    String unit = defaultString(dto.getUnit(), "st");
    BigDecimal unitPrice = dto.getUnitPrice();

    item.setDescription(dto.getDescription());
    item.setQuantity(quantity);
    item.setUnitPrice(unitPrice);
    item.setType(type);
    item.setUnit(unit);
    item.setDiscount(discount);
    item.setVat(vat);
    item.setTotal(calculateTotal(quantity, discount, vat, unitPrice));

    QuoteItem updated = repo.save(item);
    return toDTO(updated);
  }

  @Transactional
  public void deleteItem(UUID itemId) {
    QuoteItem item = repo.findById(itemId)
        .orElseThrow(() -> new RuntimeException("Item not found"));

    UUID currentCompanyId = companyService.getCurrentCompanyId();
    if (!item.getQuote().getCompany().getId().equals(currentCompanyId)) {
      throw new AccessDeniedException("Not your item");
    }

    repo.delete(item);
  }

  private QuoteItemResponseDTO toDTO(QuoteItem item) {
    QuoteItemResponseDTO out = new QuoteItemResponseDTO();
    out.setId(item.getId());
    out.setQuoteId(item.getQuote().getId());
    out.setDescription(item.getDescription());
    out.setType(item.getType());
    out.setUnit(item.getUnit());
    out.setDiscount(item.getDiscount());
    out.setVat(item.getVat());
    out.setQuantity(item.getQuantity());
    out.setUnitPrice(item.getUnitPrice());
    out.setTotal(item.getTotal());
    out.setCreatedAt(item.getCreatedAt());
    return out;
  }

  private BigDecimal calculateTotal(int quantity, int discount, int vat, BigDecimal unitPrice) {
    BigDecimal net = unitPrice
        .multiply(BigDecimal.valueOf(quantity))
        .multiply(BigDecimal.valueOf(100 - discount).divide(BigDecimal.valueOf(100)));

    return net.multiply(BigDecimal.valueOf(100 + vat).divide(BigDecimal.valueOf(100)));
  }

  private int defaultInt(Integer value, int fallback) {
    return value != null ? value : fallback;
  }

  private String defaultString(String value, String fallback) {
    return value != null && !value.isBlank() ? value : fallback;
  }
}
