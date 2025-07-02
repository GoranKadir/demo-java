package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisitAreaDTO {
  private String title;
  private String description;
  private Double areaSize;
  private BigDecimal pricePerM2;
  private List<String> images;

}