package com.kgu.life_watch.domain.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.kgu.life_watch.domain.auth.dto.request.*;
import com.kgu.life_watch.domain.auth.dto.response.*;
import com.kgu.life_watch.domain.auth.service.AuthService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;
import com.kgu.life_watch.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "AuthController", description = "인증 관련 API")
@Slf4j
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup/combined")
  @Operation(
      summary = "보호자 및 어르신 통합 회원가입 API",
      description = "한 번의 요청으로 보호자 가입과 어르신 등록을 동시에 처리합니다.(어르신 로그인 코드를 반환)")
  public ApiResponse<CombinedSignUpResponse> signUpCombined(
      @Valid @RequestBody CombinedSignUpRequest request) {
    CombinedSignUpResponse response = authService.signUpCombined(request);

    log.info(
        "[✅SUCCESS] 통합 가입 완료 - 보호자: {}, 어르신: {}",
        request.protector().name(),
        request.elderly().name());

    log.info("[✅SUCCESS] 통합 가입 완료 - 어르신 코드: {}", response.elderlyLoginCode());
    return new ApiResponse<>(response);
  }

  @PostMapping("/login")
  @Operation(summary = "로그인 API", description = "로그인 API입니다.")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    log.info("[✅SUCCESS] - 로그인 성공: 아이디={}", request.loginId());
    return new ApiResponse<>(response);
  }

  @PostMapping("/login/elder")
  @Operation(summary = "노인 코드 로그인 API", description = "노인용 고유 코드로 로그인합니다.")
  public ApiResponse<LoginResponse> loginElder(@RequestBody java.util.Map<String, String> request) {
    String loginCode = request.get("loginCode");
    LoginResponse response = authService.loginByCode(loginCode);
    log.info("[✅SUCCESS] - 노인 로그인 성공: 코드={}", loginCode);
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

  @GetMapping("/check-duplicate")
  @Operation(summary = "중복 체크 API", description = "아이디 또는 전화번호 중복 여부를 확인합니다.")
  public ApiResponse<Void> checkDuplicate(
      @RequestParam(required = false) String loginId,
      @RequestParam(required = false) String phoneNumber) {
    authService.checkDuplicate(loginId, phoneNumber);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }
}
