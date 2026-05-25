package com.kgu.life_watch.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
    @NotBlank(message = "이름은 필수입니다.") String name,
    @NotBlank(message = "전화번호는 필수입니다.") String phoneNumber,
    @NotBlank(message = "주소는 필수입니다.") String address,
    // 노인용 보호자 필드 (nullable)
    String protectorName,
    String protectorContact) {}
