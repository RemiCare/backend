package com.kgu.life_watch.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
    @NotBlank(message = "아이디는 필수 입력 값입니다.") String loginId,
    @NotBlank(message = "휴대폰 번호는 필수 입력 값입니다.") String phoneNumber,
    @NotBlank(message = "인증 코드는 필수 입력 값입니다.") String verificationCode,
    @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.") String newPassword) {}
