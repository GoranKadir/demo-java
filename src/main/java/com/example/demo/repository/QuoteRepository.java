package com.example.demo.repository;

import com.example.demo.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {
  List<Quote> findByCompanyId(UUID companyId);
  List<Quote> findByCustomerId(UUID customerId);
}