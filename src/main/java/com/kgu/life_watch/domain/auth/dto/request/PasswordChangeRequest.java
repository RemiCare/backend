package com.kgu.life_watch.domain.auth.dto.request;

public record PasswordChangeRequest(String currentPassword, String newPassword) {}
