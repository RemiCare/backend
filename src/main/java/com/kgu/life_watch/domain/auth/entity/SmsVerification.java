package com.kgu.life_watch.domain.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SmsVerification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String phoneNumber;
  private String code;

  private boolean used = false;

  private LocalDateTime createdAt;

  public SmsVerification(String phoneNumber, String code) {
    this.phoneNumber = phoneNumber;
    this.code = code;
    this.createdAt = LocalDateTime.now();
    this.used = false;
  }

  public boolean isExpired() {
    return this.createdAt.isBefore(LocalDateTime.now().minusMinutes(5));
  }

  public void markAsUsed() {
    this.used = true;
  }
}
