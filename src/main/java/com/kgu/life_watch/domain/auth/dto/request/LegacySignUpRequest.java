package com.kgu.life_watch.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LegacySignUpRequest(
    @NotBlank String name, // 어르신 성함
    @NotBlank String loginId, // 보호자 로그인 아이디
    @NotBlank String password, // 비밀번호
    @NotBlank String phoneNumber, // 어르신 휴대폰번호
    @NotBlank String address, // 어르신 거주지 주소
    @NotBlank String gender, // 어르신 성별
    @NotBlank String protectorName, // 보호자 성함
    @NotBlank String protectorContact, // 보호자 연락처
    @NotBlank String protectorAddress, // 보호자 주소
    @NotBlank String protectorGender, // 보호자 성별
    String rrn,
    String fcmToken,
    String drn,
    String role) {}
