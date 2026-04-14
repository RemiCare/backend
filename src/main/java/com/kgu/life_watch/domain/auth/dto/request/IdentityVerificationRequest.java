package com.kgu.life_watch.domain.auth.dto.request;

public record IdentityVerificationRequest(String loginId, String phoneNumber) {}
