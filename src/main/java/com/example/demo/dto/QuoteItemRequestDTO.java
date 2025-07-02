package com.example.demo.dto;

import java.time.Instant;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class QuoteItemRequestDTO {
  private UUID quoteId;
  private String description;
  private String type;
  private String unit;
  private Integer discount;
  private Integer vat;
  private int quantity;
  private BigDecimal unitPrice;

}