package com.kgu.life_watch.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;

import com.kgu.life_watch.domain.auth.entity.SmsVerification;
import com.kgu.life_watch.domain.auth.repository.SmsVerificationRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Service
@Transactional
public class AuthSmsService {

  private final SmsVerificationRepository smsVerificationRepository;
  private final DefaultMessageService messageService;
  private final String apiKey;

  @Value("${coolsms.from-number:01000000000}")
  private String fromNumber;

  public AuthSmsService(
      SmsVerificationRepository smsVerificationRepository,
      @Value("${coolsms.api-key}") String apiKey,
      @Value("${coolsms.api-secret}") String apiSecret) {
    this.smsVerificationRepository = smsVerificationRepository;
    this.apiKey = apiKey;
    this.messageService =
        NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
  }

  @Transactional
  public void sendAuthenticationCode(String phone) {
    String code;
    if (apiKey.equals("dummy")) {
      code = "1234";
    } else {
      code = String.valueOf((int) ((Math.random() * 8999) + 1000));
      Message message = new Message();
      message.setFrom(fromNumber);
      message.setTo(phone);
      message.setText("[라이프워치] 인증번호 [" + code + "]를 입력해주세요.");
      messageService.sendOne(new SingleMessageSendingRequest(message));
    }
    smsVerificationRepository.save(new SmsVerification(phone, code));
  }

  @Transactional
  public boolean verifyCode(String phoneNumber, String verificationCode) {
    SmsVerification verification =
        smsVerificationRepository
            .findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber)
            .orElseThrow(() -> LifelineException.from(ErrorCode.SMS_NOT_FOUND)); // 인증 기록 자체가 없는 경우

    //  이미 사용된 번호인지 확인
    if (verification.isUsed()) {
      throw LifelineException.from(ErrorCode.SMS_ALREADY_USED);
    }
    // 만료 시간 확인 (5분)
    if (verification.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
      throw LifelineException.from(ErrorCode.EXPIRED_SMS_CODE);
    }
    // 번호 일치 확인
    if (!verification.getCode().equals(verificationCode)) {
      throw LifelineException.from(ErrorCode.SMS_VERIFICATION_FAILED);
    }
    verification.markAsUsed();
    return true;
  }

  @Transactional(readOnly = true)
  public boolean isVerified(String phoneNumber) {
    return smsVerificationRepository
        .findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber)
        .filter(SmsVerification::isUsed)
        .isPresent();
  }
}
