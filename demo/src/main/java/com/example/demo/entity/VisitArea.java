package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class VisitArea {
  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  private CustomerVisit visit;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  private Double areaSize;
  private BigDecimal pricePerM2;
  private BigDecimal estimatedTotal;

  @ElementCollection
  @CollectionTable(name = "visit_area_images", joinColumns = @JoinColumn(name = "area_id"))
  @Column(name = "image_base64", columnDefinition = "TEXT")
  private List<String> images = new ArrayList<>();
}
