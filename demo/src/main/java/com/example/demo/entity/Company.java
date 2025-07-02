package com.example.demo.entity;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {
  @Id
  @GeneratedValue
  private UUID id;
  @Column(name = "logo", columnDefinition = "TEXT")
  private String logoBase64;
  @Column(nullable = false)
  private String name;

  private String address;
  private String phone;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String passwordHash;
  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @Builder.Default
  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Customer> customers = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Quote> quotes = new ArrayList<>();
}
