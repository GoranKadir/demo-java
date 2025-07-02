package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class CustomerVisit {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  private Customer customer;

  private Instant visitDate;
  private String representative;
  private String status;

  @Column(columnDefinition = "TEXT")
  private String generalNotes;

  @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VisitArea> areas = new ArrayList<>();

  @Column(updatable = false)
  private Instant createdAt = Instant.now();
}
