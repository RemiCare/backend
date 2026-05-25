package com.kgu.life_watch.domain.auth.dto.response;

import java.time.LocalDate;

public record LoginResponse(
    String name,
    String token,
    LocalDate birthDate,
    String protectorName,
    String protectorContact,
    String assignedProtectorName,
    String assignedProtectorPhone,
    Long userId,
    boolean isProtector,
    String role,
    String loginCode,
    String phoneNumber,
    String address,
    String assignedElderName,
    String assignedElderCode) {}
