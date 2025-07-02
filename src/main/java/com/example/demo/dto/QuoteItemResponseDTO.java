package com.example.demo.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class QuoteItemResponseDTO {
  private UUID id;
  private UUID quoteId;
  private String description;
  private String type;
  private String unit;
  private Integer discount;
  private Integer vat;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal total;
  private Instant createdAt;
}