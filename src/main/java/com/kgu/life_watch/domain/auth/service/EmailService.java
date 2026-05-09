package com.kgu.life_watch.domain.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  public void sendEmailCode(String to, String code) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("[라이프워치] 비밀번호 재설정 인증번호입니다.");
    message.setText("안녕하세요. 라이프워치입니다.\n\n비밀번호 재설정을 위한 인증번호는 [" + code + "] 입니다.\n5분 이내에 입력해주세요.");

    mailSender.send(message);
  }
}
