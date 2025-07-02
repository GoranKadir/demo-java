package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Company;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Quote;
import com.example.demo.entity.QuoteItem;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.QuoteItemRepository;
import com.example.demo.repository.QuoteRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuoteService {

  private final QuoteRepository quoteRepo;
  private final QuoteItemRepository itemRepo;

  private final CompanyRepository companyRepo;
  private final CustomerRepository customerRepo;

  private final CompanyService companyService;

  private final CustomerService customerService;

  public QuoteService(QuoteRepository quoteRepo,
      QuoteItemRepository itemRepo,
      CompanyRepository companyRepo,
      CustomerRepository customerRepo, CompanyService companyService,
      CustomerService customerService) {
    this.quoteRepo = quoteRepo;
    this.itemRepo = itemRepo;
    this.companyRepo = companyRepo;
    this.customerRepo = customerRepo;
    this.companyService = companyService;
    this.customerService = customerService;
  }

  @Transactional
  public QuoteResponseDTO createDraft(UUID companyId) {
    Company company = companyRepo.findById(companyId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    // Skapa en helt tom Quote‐entitet med bara company och status
    Quote draft = new Quote();
    draft.setCompany(company);
    draft.setStatus("draft");
    // sparar draft utan att fylla i något annat
    Quote saved = quoteRepo.save(draft);

    return toResponseDTO(saved);
  }

  @Transactional
  public QuoteResponseDTO createQuote(QuoteRequestDTO dto, UUID companyId) {
    // Hämta Company-entitet
    Company company = companyRepo.findById(companyId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    // Hämta Customer-entitet och kontrollera ägarskap
    Customer customer = customerRepo.findById(dto.getCustomerId())
        .filter(c -> c.getCompany().getId().equals(companyId))
        .orElseThrow(() -> new RuntimeException(
            "Customer not found or does not belong to company"));

    // Bygg och spara Quote med riktiga relationer
    Quote quote = Quote.builder()
        .company(company)
        .customer(customer)
        .quoteNumber(dto.getQuoteNumber())
        .quoteDate(dto.getQuoteDate())
        .validUntil(dto.getValidUntil())
        .paymentTerms(dto.getPaymentTerms())
        .status("draft")
        .build();

    Quote saved = quoteRepo.save(quote);

    return toResponseDTO(saved);
  }

  @Transactional
  public QuoteResponseDTO createOrUpdateQuote(QuoteRequestDTO dto, UUID companyId) {
    Company company = companyRepo.findById(companyId)
        .orElseThrow(() -> new RuntimeException("Company not found"));
    Customer customer = customerRepo.findById(dto.getCustomerId())
        .filter(c -> c.getCompany().getId().equals(companyId))
        .orElseThrow(() -> new RuntimeException("Customer not found"));

    final Quote quote;
    if (dto.getId() != null && quoteRepo.existsById(dto.getId())) {
      quote = quoteRepo.findById(dto.getId())
          .orElseThrow(() -> new RuntimeException("Quote not found"));
    } else {
      quote = new Quote();
      quote.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
      quote.setCompany(company);
      // quote.setCustomer(customer);    <-- ta bort från här
      quote.setStatus("draft");
    }

    // Sätt alltid customer, även vid update
    quote.setCustomer(customer);

    quote.setQuoteNumber(dto.getQuoteNumber());
    quote.setQuoteDate(dto.getQuoteDate());
    quote.setValidUntil(dto.getValidUntil());
    quote.setPaymentTerms(dto.getPaymentTerms());
    quote.setWorkDescription(dto.getWorkDescription());
    quote.setUseRot(dto.isUseRot());
    quote.setRotPercent(dto.getRotPercent());
    quote.setTerms(dto.getTerms() != null ? dto.getTerms() : new ArrayList<>());

    // 4) Synka items om sådana skickas
    if (dto.getItems() != null) {
      quote.getItems().clear();
      for (QuoteItemRequestDTO itemDto : dto.getItems()) {
        QuoteItem item = new QuoteItem();
        item.setQuote(quote);
        item.setDescription(itemDto.getDescription());
        item.setQuantity(itemDto.getQuantity());
        item.setUnitPrice(itemDto.getUnitPrice());
        item.setType(itemDto.getType() != null ? itemDto.getType() : "Material");
        item.setUnit(itemDto.getUnit() != null ? itemDto.getUnit() : "st");
        item.setDiscount(itemDto.getDiscount() != null ? itemDto.getDiscount() : 0);
        item.setVat(itemDto.getVat() != null ? itemDto.getVat() : 0);

        item.setTotal(calculateTotal(itemDto));

        quote.getItems().add(item);
      }
      BigDecimal rotAmount = calculateRotAmount(quote);
      quote.setRotAmount(rotAmount); // om du vill returnera det
    }

    Quote saved = quoteRepo.save(quote);
    return toResponseDTO(saved);
  }

  private BigDecimal calculateTotal(QuoteItemRequestDTO dto) {
    BigDecimal unitPrice = dto.getUnitPrice();
    int quantity = defaultInt(dto.getQuantity(), 1);
    int discount = defaultInt(dto.getDiscount(), 0);
    int vat = defaultInt(dto.getVat(), 0);

    BigDecimal net = unitPrice
        .multiply(BigDecimal.valueOf(quantity))
        .multiply(BigDecimal.valueOf(100 - discount).divide(BigDecimal.valueOf(100)));

    return net.multiply(BigDecimal.valueOf(100 + vat).divide(BigDecimal.valueOf(100)));
  }

  private BigDecimal calculateRotAmount(Quote quote) {
    if (!quote.isUseRot()) {
      return BigDecimal.ZERO;
    }

    BigDecimal rotTotal = quote.getItems().stream()
        .filter(item -> "Arbete".equals(item.getType()))
        .map(QuoteItem::getTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal percent = BigDecimal.valueOf(quote.getRotPercent()).divide(BigDecimal.valueOf(100));
    BigDecimal deduction = rotTotal.multiply(percent);
    return deduction.min(BigDecimal.valueOf(50000));
  }

  private BigDecimal calculateGrandTotalWithRot(Quote quote) {
    BigDecimal total = quote.getItems().stream()
        .map(QuoteItem::getTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return total.subtract(calculateRotAmount(quote));
  }

  private int defaultInt(Integer value, int fallback) {
    return value != null ? value : fallback;
  }

  @Transactional(readOnly = true)
  public List<QuoteResponseDTO> listQuotes(UUID companyId) {
    return quoteRepo.findByCompanyId(companyId).stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public QuoteResponseDTO getQuote(UUID quoteId, String principalName) {
    // 1) Hämta företaget baserat på e-post
    UUID companyId = companyService.findDtoByEmail(principalName)
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    // 2) Hämta offerten och säkerställ att ägarskapet stämmer
    Quote q = quoteRepo.findById(quoteId)
        .filter(quote -> quote.getCompany().getId().equals(companyId))
        .orElseThrow(() -> new RuntimeException("Quote not found for this company"));

    return toResponseDTO(q);
  }

  @Transactional
  public void deleteDraft(UUID quoteId, String principalName) {
    UUID companyId = companyService.findDtoByEmail(principalName)
        .map(CompanyDTO::getId)
        .orElseThrow(() -> new RuntimeException("Company not found"));

    // kontrollera att det är en draft som ägs av företaget
    Quote draft = quoteRepo.findById(quoteId)
        .filter(q -> q.getCompany().getId().equals(companyId) && "draft".equals(q.getStatus()))
        .orElseThrow(() -> new RuntimeException("Draft not found"));

    quoteRepo.delete(draft);
  }

  private QuoteResponseDTO toResponseDTO(Quote q) {
    QuoteResponseDTO out = new QuoteResponseDTO();
    out.setId(q.getId());

    // Hämta companyId via relationen
    out.setCompanyId(q.getCompany().getId());

    // Hämta full CustomerDTO via relationen
    // Om kund hiljt inte valts, låt customerId vara null
    if (q.getCustomer() != null) {
      out.setCustomerId(q.getCustomer().getId());
    } else {
      out.setCustomerId(null);
    }

    out.setQuoteNumber(q.getQuoteNumber());
    out.setQuoteDate(q.getQuoteDate());
    out.setValidUntil(q.getValidUntil());
    out.setPaymentTerms(q.getPaymentTerms());
    out.setStatus(q.getStatus());
    out.setCreatedAt(q.getCreatedAt());
    out.setUpdatedAt(q.getUpdatedAt());
    out.setWorkDescription(q.getWorkDescription());
    out.setUseRot(q.isUseRot());
    out.setRotPercent(q.getRotPercent());
    out.setRotAmount(q.getRotAmount());
    out.setGrandTotal(calculateGrandTotalWithRot(q));

    // Om du vill använda relationen items istället för repo-anrop:
    out.setItems(q.getItems().stream()
        .map(this::toItemDTO)
        .collect(Collectors.toList()));
    out.setTerms(q.getTerms());
    return out;
  }

  private QuoteItemResponseDTO toItemDTO(QuoteItem item) {
    QuoteItemResponseDTO out = new QuoteItemResponseDTO();
    out.setId(item.getId());

    // Hämta quoteId via relationen
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
}