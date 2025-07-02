package com.example.demo.repository;

import com.example.demo.entity.Customer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
  List<Customer> findByCompanyId(UUID companyId);
}