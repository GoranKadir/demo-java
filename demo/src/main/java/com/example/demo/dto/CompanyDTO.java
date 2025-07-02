package com.example.demo.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CompanyDTO {
  private UUID id;
  private String logoBase64;
  private String name;
  private String address;
  private String phone;
  private String email;
  private Instant createdAt;
}