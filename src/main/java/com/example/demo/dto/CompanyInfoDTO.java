package com.example.demo.dto;


import lombok.Data;

@Data
public class CompanyInfoDTO {
  private String name;
  private String address;
  private String phone;
  private String email;
  private String password;
  private String logoBase64;
}
