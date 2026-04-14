package com.kgu.life_watch.domain.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.*;
import com.kgu.life_watch.domain.auth.dto.response.LoginResponse;
import com.kgu.life_watch.domain.auth.service.AuthService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;
import com.kgu.life_watch.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "AuthController", description = "인증 관련 API")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  @Operation(summary = "노인 회원가입 API", description = "노인 회원가입 API입니다.")
  public ApiResponse<Void> signUp(@Valid @RequestBody ElderlySignUpRequest request) {
    authService.signUpElderly(request);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PostMapping("/signup/social-worker")
  @Operation(summary = "사회복지사 회원가입 API", description = "사회복지사 회원가입 API입니다.")
  public ApiResponse<Void> signUpSocialWorker(
      @Valid @RequestBody SocialWorkerSignUpRequest request) {
    authService.signUpSocialWorker(request);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PostMapping("/login")
  @Operation(summary = "로그인 API", description = "로그인 API입니다.")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return new ApiResponse<>(response);
  }

  @PatchMapping("/fcm-token")
  @Operation(summary = "FCM 토큰 갱신 API", description = "사용자의 FCM 토큰을 최신값으로 갱신합니다.")
  public ApiResponse<Void> updateFcmToken(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody FcmTokenUpdateRequest request) {
    authService.updateFcmToken(userDetails.user().getId(), request.fcmToken());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PostMapping("/verify-identity")
  @Operation(summary = "아이디+전화번호 본인 확인", description = "아이디와 전화번호가 일치하는 사용자가 존재하는지 확인합니다.")
  public ApiResponse<Void> verifyIdentity(@RequestBody IdentityVerificationRequest request) {
    authService.verifyIdentity(request.loginId(), request.phoneNumber());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }
}
