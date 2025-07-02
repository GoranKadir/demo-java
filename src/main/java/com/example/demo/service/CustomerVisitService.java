package com.example.demo.service;

import com.example.demo.dto.CustomerVisitDTO;
import com.example.demo.dto.VisitAreaDTO;
import com.example.demo.entity.Company;
import com.example.demo.entity.Customer;
import com.example.demo.entity.CustomerVisit;
import com.example.demo.entity.VisitArea;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.CustomerVisitRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerVisitService {

  private final CustomerVisitRepository visitRepository;
  private final CustomerRepository customerRepository;

  private final CompanyService companyService;

  public CustomerVisitService(CustomerVisitRepository visitRepository, CustomerRepository customerRepository,
      CompanyService companyService) {
    this.visitRepository = visitRepository;
    this.customerRepository = customerRepository;
    this.companyService = companyService;
  }

  @Transactional
  public CustomerVisit createVisit(CustomerVisitDTO dto, UUID loggedInCompanyId) {
    Customer customer = customerRepository.findById(dto.getCustomerId())
        .orElseThrow(() -> new RuntimeException("Kund hittades inte"));

    if (!customer.getCompany().getId().equals(loggedInCompanyId)) {
      throw new AccessDeniedException("Du får inte skapa möten för en kund som inte tillhör ditt företag.");
    }

    CustomerVisit visit = new CustomerVisit();
    visit.setCustomer(customer);
    visit.setCompany(customer.getCompany()); // redundant, men tydliggör sambandet
    visit.setVisitDate(dto.getVisitDate());
    visit.setRepresentative(dto.getRepresentative());
    visit.setStatus(dto.getStatus());
    visit.setGeneralNotes(dto.getGeneralNotes());

    List<VisitArea> areaEntities = new ArrayList<>();
    for (VisitAreaDTO areaDto : dto.getAreas()) {
      VisitArea area = new VisitArea();
      area.setVisit(visit);
      area.setTitle(areaDto.getTitle());
      area.setDescription(areaDto.getDescription());
      area.setAreaSize(areaDto.getAreaSize());
      area.setPricePerM2(areaDto.getPricePerM2());

      if (areaDto.getAreaSize() != null && areaDto.getPricePerM2() != null) {
        BigDecimal estimated = areaDto.getPricePerM2()
            .multiply(BigDecimal.valueOf(areaDto.getAreaSize()));
        area.setEstimatedTotal(estimated);
      }

      area.setImages(areaDto.getImages() != null ? areaDto.getImages() : new ArrayList<>());
      areaEntities.add(area);
    }

    visit.setAreas(areaEntities);
    return visitRepository.save(visit);
  }

  @Transactional
  public void update(UUID id, CustomerVisitDTO dto, UUID loggedInCompanyId) {
    CustomerVisit visit = visitRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Visit not found"));

    // Kontrollera att företaget äger besöket
    if (!visit.getCompany().getId().equals(loggedInCompanyId)) {
      throw new AccessDeniedException("Du har inte behörighet att ändra detta kundmöte.");
    }

    // Hämta och verifiera kunden
    Customer customer = customerRepository.findById(dto.getCustomerId())
        .orElseThrow(() -> new RuntimeException("Customer not found"));

    if (!customer.getCompany().getId().equals(loggedInCompanyId)) {
      throw new AccessDeniedException("Kunden tillhör inte ditt företag.");
    }

    visit.setCustomer(customer);
    visit.setVisitDate(dto.getVisitDate());
    visit.setRepresentative(dto.getRepresentative());
    visit.setStatus(dto.getStatus());
    visit.setGeneralNotes(dto.getGeneralNotes());

    // Rensa gamla områden (orphanRemoval = true hanterar borttagning)
    visit.getAreas().clear();

    // Lägg till nya områden
    for (VisitAreaDTO areaDto : dto.getAreas()) {
      VisitArea area = new VisitArea();
      area.setVisit(visit);
      area.setTitle(areaDto.getTitle());
      area.setDescription(areaDto.getDescription());
      area.setAreaSize(areaDto.getAreaSize());
      area.setPricePerM2(areaDto.getPricePerM2());

      if (areaDto.getAreaSize() != null && areaDto.getPricePerM2() != null) {
        BigDecimal estimated = areaDto.getPricePerM2()
            .multiply(BigDecimal.valueOf(areaDto.getAreaSize()));
        area.setEstimatedTotal(estimated);
      }

      area.setImages(areaDto.getImages() != null ? areaDto.getImages() : new ArrayList<>());
      visit.getAreas().add(area);
    }
    // Sparas automatiskt tack vare @Transactional
  }

  public CustomerVisit createDraftVisit(UUID companyId) {
    Company company = companyService.findEntityById(companyId);

    CustomerVisit visit = new CustomerVisit();
    visit.setCompany(company);
    visit.setStatus("draft");
    visit.setCreatedAt(Instant.now());

    return visitRepository.save(visit);
  }

  public List<CustomerVisitDTO> getVisitsForCompany(UUID companyId) {
    List<CustomerVisit> visits = visitRepository.findByCompanyId(companyId);
    return visits.stream().map(this::toDTO).toList();
  }

  public CustomerVisitDTO getVisitById(UUID id, UUID companyId) {
    CustomerVisit visit = findById(id);
    if (!visit.getCompany().getId().equals(companyId)) {
      throw new AccessDeniedException("Du har inte tillgång till detta besök");
    }
    return toDTO(visit);
  }

  private CustomerVisitDTO toDTO(CustomerVisit visit) {
    CustomerVisitDTO dto = new CustomerVisitDTO();
    dto.setId(visit.getId());
    dto.setCustomerId(visit.getCustomer() != null ? visit.getCustomer().getId() : null);
    dto.setVisitDate(visit.getVisitDate());
    dto.setRepresentative(visit.getRepresentative());
    dto.setStatus(visit.getStatus());
    dto.setGeneralNotes(visit.getGeneralNotes());

    List<VisitAreaDTO> areaDtos = visit.getAreas().stream().map(area -> {
      VisitAreaDTO a = new VisitAreaDTO();
      a.setTitle(area.getTitle());
      a.setDescription(area.getDescription());
      a.setAreaSize(area.getAreaSize());
      a.setPricePerM2(area.getPricePerM2());
      a.setImages(area.getImages());
      return a;
    }).toList();

    dto.setAreas(areaDtos);
    return dto;
  }

  public CustomerVisit save(CustomerVisit visit) {
    return visitRepository.save(visit);
  }

  public List<CustomerVisit> findByCompanyId(UUID companyId) {
    return visitRepository.findByCompanyId(companyId);
  }

  public CustomerVisit findById(UUID id) {
    return visitRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Kundmöte hittades inte"));
  }

}