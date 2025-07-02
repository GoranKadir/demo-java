package com.example.demo.dto;

import lombok.Data;

@Data
public class AuthResponse {
  private String token;       // om du kör JWT
  private CompanyDTO company; // företagets data
}