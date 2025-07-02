package com.example.demo.dto;

import java.math.BigDecimal;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class QuoteResponseDTO {
  private UUID id;
  private UUID companyId;
  private UUID customerId;
  private String quoteNumber;
  private LocalDate quoteDate;
  private LocalDate validUntil;
  private String paymentTerms;
  private String status;
  private Instant createdAt;
  private Instant updatedAt;
  private String workDescription;
  private boolean useRot;
  private int rotPercent;
  private BigDecimal rotAmount;
  private BigDecimal grandTotal;
  private List<QuoteItemResponseDTO> items;
  private List<String> terms;

}