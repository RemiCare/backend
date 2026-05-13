package com.kgu.life_watch.domain.auth.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LegacySignUpRequest(
    @NotBlank String name, // 어르신 성함
    @NotBlank String loginId, // 보호자 로그인 아이디
    @NotBlank @Email String email, // 보호자 이메일
    @NotBlank String password, // 비밀번호
    @NotBlank String phoneNumber, // 전화번호
    @NotBlank String address, // 주소
    @NotBlank String protectorName, // 보호자 성함
    @NotBlank String protectorContact, // 보호자 연락처
    String rrn, // 주민등록번호
    @NotNull @JsonFormat(pattern = "yyyy/MM/dd") LocalDate birthDate,
    String gender,
    String fcmToken,
    String drn,
    String role) {}
