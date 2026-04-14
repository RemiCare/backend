package com.kgu.life_watch.domain.auth.dto.response;

import java.time.LocalDate;

public record LoginResponse(
    String name,
    String token,
    LocalDate birthDate,
    String protectorName,
    String protectorContact,
    String socialWorkerName,
    String socialWorkerPhone,
    Long userId,
    boolean isSocialWorker,
    String phoneNumber,
    String address) {}
