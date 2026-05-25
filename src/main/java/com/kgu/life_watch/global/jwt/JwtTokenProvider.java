package com.kgu.life_watch.global.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import com.kgu.life_watch.domain.user.entity.User;

@Component
public class JwtTokenProvider {

  private final Key key;
  private final long expiration;

  public JwtTokenProvider(
      @Value("${spring.jwt.secret}") String secretKey,
      @Value("${spring.jwt.access-token-expiration}") long expiration) {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    this.expiration = expiration;
  }

  public String generateToken(User user) {
    // 토큰 생성 (loginId + role 포함)
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .setSubject(user.getLoginId())
        .claim("role", user.getRole().name())
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String getLoginIdFromToken(String token) {
    // 토큰에서 사용자 ID 추출
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public boolean validateToken(String token) {
    try {
      // 토큰 파싱 → 유효한 경우 true
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }
}
