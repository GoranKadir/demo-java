package com.example.demo.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "quotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  // Kund kan vara null för en helt tom draft
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "customer_id", nullable = true)
  private Customer customer;

  @Column(name = "quote_number", nullable = true)
  private String quoteNumber;

  @Column(name = "quote_date", nullable = true)
  private LocalDate quoteDate;

  @Column(name = "valid_until", nullable = true)
  private LocalDate validUntil;

  @Column(name = "payment_terms", nullable = true)
  private String paymentTerms;

  @Column(nullable = false)
  private String status; // t.ex. "draft", "sent", "accepted"

  @Column(columnDefinition = "TEXT")
  private String workDescription;

  @Column(name = "use_rot")
  private boolean useRot;

  @Column(name = "rot_percent")
  private int rotPercent; // 50, 30, 20

  @Column(name = "rot_amount")
  private BigDecimal rotAmount = BigDecimal.ZERO;
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Version
  private Long version;

  @Builder.Default
  @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<QuoteItem> items = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "quote_terms", joinColumns = @JoinColumn(name = "quote_id"))
  @Column(name = "term", nullable = false)
  private List<String> terms = new ArrayList<>();
}