package com.kgu.life_watch.domain.auth.dto.request;

public record SmsVerificationRequest(String phoneNumber, String verificationCode) {}
