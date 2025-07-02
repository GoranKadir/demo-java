package com.example.demo.repository;

import com.example.demo.entity.Company;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
  Optional<Company> findByEmail(String email);
}
