package com.kgu.life_watch.domain.auth.dto.request;

public record ResetPasswordRequest(String loginId, String newPassword, String verificationCode) {}
