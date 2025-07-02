package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.service.CompanyService;
import com.example.demo.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthenticationManager authManager;
  private final CompanyService companyService;
  private final JwtUtil jwtUtil;

  public AuthController(AuthenticationManager authManager,
      CompanyService companyService,
      JwtUtil jwtUtil) {
    this.authManager = authManager;
    this.companyService = companyService;
    this.jwtUtil = jwtUtil;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
    var authToken = new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());
    authManager.authenticate(authToken);
    var company = companyService.findByEmail(req.getEmail())
        .orElseThrow(() -> new RuntimeException("Företag saknas"));
    String jwt = jwtUtil.generateToken(company.getEmail());
    var resp = new AuthResponse();
    resp.setToken(jwt);
    resp.setCompany(companyService.toDTO(company));
    return ResponseEntity.ok(resp);
  }
}