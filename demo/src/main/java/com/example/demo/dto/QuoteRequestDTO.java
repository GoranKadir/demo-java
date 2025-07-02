package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class QuoteRequestDTO {
  private UUID id;
  private UUID customerId;
  private String quoteNumber;
  private LocalDate quoteDate;
  private LocalDate validUntil;
  private String paymentTerms;
  private String workDescription;
  private boolean useRot;
  private int rotPercent;
  private BigDecimal rotAmount;
  private List<QuoteItemRequestDTO> items;
  private List<String> terms;

}