package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

  private final Key key;
  private final long expirationMs;

  public JwtUtil(@Value("${jwt.secret}") String base64Secret,
      @Value("${jwt.expiration}") long expirationMs) {
    // Decode Base64 till råa bytes och gör en Key som är tillräckligt lång
    byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.expirationMs = expirationMs;
  }

  public String generateToken(String subject) {
    Date now = new Date();
    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + expirationMs))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String extractSubject(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims,T> resolver) {
    final Claims claims = Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
    return resolver.apply(claims);
  }

  public boolean validateToken(String token, String subject) {
    final String tokSub = extractSubject(token);
    return tokSub.equals(subject) && !extractClaim(token, Claims::getExpiration).before(new Date());
  }
}