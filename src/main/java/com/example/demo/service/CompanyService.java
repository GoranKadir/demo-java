  package com.example.demo.service;


  import com.example.demo.dto.CompanyDTO;
  import com.example.demo.dto.CompanyInfoDTO;
  import com.example.demo.entity.Company;
  import com.example.demo.repository.CompanyRepository;
  import com.example.demo.util.CurrentUser;
  import java.util.Optional;
  import java.util.UUID;
  import org.springframework.security.crypto.password.PasswordEncoder;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;

  @Service
  public class CompanyService {
    private final CompanyRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CompanyService(CompanyRepository repository, PasswordEncoder passwordEncoder) {
      this.repository = repository;
      this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UUID getCurrentCompanyId() {
      String email = CurrentUser.getEmail();
      return repository.findByEmail(email)
          .map(Company::getId)
          .orElseThrow(() -> new RuntimeException("Company not found for email: " + email));
    }
    @Transactional
    public Optional<Company> findByEmail(String email) {
      return repository.findByEmail(email);
    }

    @Transactional
    public CompanyDTO createCompany(CompanyInfoDTO info) {
      Company entity = Company.builder()
          .name(info.getName())
          .address(info.getAddress())
          .phone(info.getPhone())
          .email(info.getEmail())
          .passwordHash(passwordEncoder.encode(info.getPassword()))
          .logoBase64(info.getLogoBase64())
          .build();
      Company saved = repository.save(entity);
      return toDTO(saved);
    }

    @Transactional
    public CompanyDTO updateCompany(UUID id, CompanyInfoDTO info) {
      Company entity = repository.findById(id)
          .orElseThrow(() -> new RuntimeException("Company not found"));
      entity.setName(info.getName());
      entity.setAddress(info.getAddress());
      entity.setPhone(info.getPhone());
      entity.setEmail(info.getEmail());
      // Om en ny password skickas med, uppdatera hash
      if (info.getPassword() != null && !info.getPassword().isBlank()) {
        entity.setPasswordHash(passwordEncoder.encode(info.getPassword()));
      }
      entity.setLogoBase64(info.getLogoBase64());
      Company updated = repository.save(entity);
      return toDTO(updated);
    }

    @Transactional(readOnly = true)
    public Optional<CompanyDTO> findDtoByEmail(String email) {
      return repository.findByEmail(email)
          .map(this::toDTO);
    }

    @Transactional
    public void updateLogo(UUID companyId, String base64) {
      Company company = repository.findById(companyId)
          .orElseThrow(() -> new RuntimeException("Company not found"));
      company.setLogoBase64(base64);
      repository.save(company);
    }

    @Transactional(readOnly = true)
    public String getLogoBase64(UUID companyId) {
      return repository.findById(companyId)
          .map(Company::getLogoBase64)
          .orElse(null);
    }

    public Company findEntityById(UUID id) {
      return repository.findById(id)
          .orElseThrow(() -> new RuntimeException("Företag hittades inte"));
    }

    public CompanyDTO toDTO(Company c) {
      CompanyDTO dto = new CompanyDTO();
      dto.setId(c.getId());
      dto.setLogoBase64(c.getLogoBase64());
      dto.setName(c.getName());
      dto.setAddress(c.getAddress());
      dto.setPhone(c.getPhone());
      dto.setEmail(c.getEmail());
      dto.setCreatedAt(c.getCreatedAt());
      return dto;
    }
  }