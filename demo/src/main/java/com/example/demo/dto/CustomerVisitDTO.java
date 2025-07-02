package com.example.demo.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerVisitDTO {
  private UUID id;
  private UUID customerId;
  private Instant visitDate;
  private String representative;
  private String status;
  private String generalNotes;
  private List<VisitAreaDTO> areas;

}