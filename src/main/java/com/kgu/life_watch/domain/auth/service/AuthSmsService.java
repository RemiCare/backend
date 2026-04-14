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

@Service
@Transactional
public class AuthSmsService {

  private final SmsVerificationRepository smsVerificationRepository;
  private final DefaultMessageService messageService;

  public AuthSmsService(
      SmsVerificationRepository smsVerificationRepository,
      @Value("${coolsms.api-key}") String apiKey,
      @Value("${coolsms.api-secret}") String apiSecret) {
    this.smsVerificationRepository = smsVerificationRepository;
    this.messageService =
        NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
  }

  @Transactional
  public void sendAuthenticationCode(String phone) {
    String code = String.valueOf((int) ((Math.random() * 8999) + 1000));

    Message message = new Message();
    message.setFrom("01031474612");
    message.setTo(phone);
    message.setText("[라이프워치] 인증번호 [" + code + "]를 입력해주세요.");

    messageService.sendOne(new SingleMessageSendingRequest(message));
    smsVerificationRepository.save(new SmsVerification(phone, code));
  }

  @Transactional
  public boolean verifyCode(String phoneNumber, String verificationCode) {
    return smsVerificationRepository
        .findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber)
        .filter(verification -> !verification.isUsed())
        .filter(
            verification ->
                verification.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(5)))
        .filter(verification -> verification.getCode().equals(verificationCode))
        .map(
            verification -> {
              verification.markAsUsed();
              return true;
            })
        .orElse(false);
  }

  @Transactional(readOnly = true)
  public boolean isVerified(String phoneNumber) {
    return smsVerificationRepository
        .findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber)
        .filter(SmsVerification::isUsed)
        .isPresent();
  }
}
