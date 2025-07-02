package com.example.demo.repository;

import com.example.demo.entity.CustomerVisit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerVisitRepository extends JpaRepository<CustomerVisit, UUID> {

  List<CustomerVisit> findByCompanyId(UUID companyId);
}
