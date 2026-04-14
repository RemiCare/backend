package com.kgu.life_watch.domain.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.auth.entity.SmsVerification;

public interface SmsVerificationRepository extends JpaRepository<SmsVerification, Long> {
  Optional<SmsVerification> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

  void deleteByUsedIsTrueAndCreatedAtBefore(LocalDateTime threshold);
}
