package com.example.demo.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class CurrentUser {

  public static String getEmail() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof UserDetails userDetails) {
      return userDetails.getUsername();
    } else if (principal instanceof String email) {
      return email; // fallback, kan förekomma i tester
    }
    throw new RuntimeException("Unknown principal type: " + principal);
  }
}