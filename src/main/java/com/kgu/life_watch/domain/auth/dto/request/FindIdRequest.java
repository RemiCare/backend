package com.kgu.life_watch.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FindIdRequest(
    @NotBlank(message = "이름은 필수 입력 값입니다.") String name,
    @NotBlank(message = "휴대폰 번호는 필수 입력 값입니다.") String phoneNumber) {}
