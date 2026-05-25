package com.kgu.life_watch.domain.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.FindIdRequest;
import com.kgu.life_watch.domain.auth.dto.request.FindPasswordRequest;
import com.kgu.life_watch.domain.auth.dto.request.PasswordChangeRequest;
import com.kgu.life_watch.domain.auth.dto.request.ResetPasswordRequest;
import com.kgu.life_watch.domain.auth.dto.response.FindLoginIdResponse;
import com.kgu.life_watch.domain.auth.service.PasswordService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;
import com.kgu.life_watch.global.security.CustomUserDetails;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "AuthPasswordController", description = "비밀번호/아이디 찾기 및 변경 관련 API")
public class AuthPasswordController {

  private final PasswordService passwordService;

  @Operation(summary = "비밀번호 변경 API", description = "기존 비밀번호를 확인하고 새 비밀번호로 변경합니다.")
  @PatchMapping("/password/change")
  public ApiResponse<Void> changePassword(
      @RequestBody @Valid PasswordChangeRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    passwordService.changePassword(request, userDetails.user());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @Operation(summary = "아이디 찾기 API", description = "이름과 전화번호를 통해 로그인 ID(아이디)를 조회합니다.")
  @PostMapping("/find-id")
  public ApiResponse<FindLoginIdResponse> findLoginId(@RequestBody @Valid FindIdRequest request) {
    String loginId = passwordService.findLoginId(request);
    return new ApiResponse<>(FindLoginIdResponse.builder().loginId(loginId).build());
  }

  @Operation(summary = "비밀번호 찾기 인증번호 전송 API", description = "로그인 ID와 전화번호를 통해 인증번호를 전송합니다.")
  @PostMapping("/find-password")
  public ApiResponse<Void> sendResetCode(@RequestBody @Valid FindPasswordRequest request) {
    passwordService.sendPasswordResetCode(request);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @Operation(summary = "비밀번호 재설정 API", description = "인증번호를 검증하고 새 비밀번호로 변경합니다.")
  @PatchMapping("/reset-password")
  public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
    passwordService.resetPassword(request);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }
}
