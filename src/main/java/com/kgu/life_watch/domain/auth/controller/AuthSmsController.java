package com.kgu.life_watch.domain.auth.controller;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.SmsVerificationRequest;
import com.kgu.life_watch.domain.auth.dto.response.SmsVerifyResponse;
import com.kgu.life_watch.domain.auth.service.AuthSmsService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "AuthSmsController", description = "인증(메시지) 관련 API")
public class AuthSmsController {

  private final AuthSmsService smsService;

  // 인증 메세지 발송
  @GetMapping("/sms")
  @Operation(summary = "인증 메시지 발송 API", description = "인증 메시지를 발송하는 API입니다.")
  public ApiResponse<Void> sendSms(@RequestParam String phone) {
    smsService.sendAuthenticationCode(phone);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  // 인증
  @PostMapping("/sms/verify")
  @Operation(summary = "인증 메시지 검증 API", description = "인증 메시지를 검증하는 API입니다.")
  public ApiResponse<SmsVerifyResponse> verifyCode(@RequestBody SmsVerificationRequest request) {
    boolean isValid = smsService.verifyCode(request.phoneNumber(), request.verificationCode());
    return new ApiResponse<>(SmsVerifyResponse.builder().isValid(isValid).build());
  }
}
