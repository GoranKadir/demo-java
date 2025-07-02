package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quote_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteItem {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "quote_id", nullable = false)
  private Quote quote;
  @Column(nullable = false)
  private String type; // Material eller Arbete
  @Column(nullable = false)
  private String unit;
  @Column(nullable = false)
  private String description;
  private Integer discount;
  @Column(nullable = false)
  private Integer vat;
  @Column(nullable = false)
  private Integer quantity;
  @Column(name = "unit_price", nullable = false)
  private BigDecimal unitPrice;
  @Column(nullable = false)
  private BigDecimal total; // quantity * unitPrice

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;
}