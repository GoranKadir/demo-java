package com.example.demo.config;

import com.example.demo.repository.CompanyRepository;
import com.example.demo.util.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {

  // 1) Hash/verify-bean
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 2) Ladda Company som UserDetails (användarinfo)
  @Bean
  public UserDetailsService userDetailsService(CompanyRepository repo) {
    return username -> repo.findByEmail(username)
        .map(company -> User.builder()
            .username(company.getEmail())
            .password(company.getPasswordHash())
            .roles("COMPANY")        // kan justeras efter behov
            .build()
        )
        .orElseThrow(() -> new RuntimeException("Company not found: " + username));
  }

  // 3) Gör AuthenticationManager tillgänglig för AuthController
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  // 4) HTTP-säkerhet
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http,
      JwtAuthenticationFilter jwtFilter) throws Exception {
    http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // Tillåt inloggning, H2-console och registrering av företag
            .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/companies").permitAll()
            .requestMatchers("/api/pdf/**").permitAll()
            // Alla andra anrop kräver giltig JWT
            .anyRequest().authenticated()
        )
        .headers(headers -> headers.frameOptions(frame -> frame.disable()))
        // Lägg på JWT‐filtret
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
