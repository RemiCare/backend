package com.kgu.life_watch.domain.auth.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.repository.SmsVerificationRepository;

@Component
@RequiredArgsConstructor
public class AuthSmsCleanupScheduler {

  private final SmsVerificationRepository smsVerificationRepository;

  // 매일 새벽 3시: used == true 이고 하루가 지난 인증번호 삭제 -> db 부담 줄이기 위해서 삭제합니두~
  @Scheduled(cron = "0 0 3 * * *")
  @Transactional
  public void cleanupExpiredSmsVerifications() {
    LocalDateTime threshold = LocalDateTime.now().minusDays(1);
    smsVerificationRepository.deleteByUsedIsTrueAndCreatedAtBefore(threshold);
  }
}
