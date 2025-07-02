package com.example.demo.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CustomerDTO {
  private UUID id;
  private UUID companyId;
  private String name;
  private String address;
  private String phone;
  private String email;
  private Instant createdAt;
}