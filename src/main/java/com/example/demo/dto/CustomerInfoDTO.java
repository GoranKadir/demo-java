package com.example.demo.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CustomerInfoDTO {
  private UUID companyId;
  private String name;
  private String address;
  private String phone;
  private String email;
}