package com.kgu.life_watch.domain.auth.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

public record ElderlySignUpRequest(
    @NotBlank(message = "이름은 필수입니다.") String name,
    @NotBlank(message = "아이디는 필수입니다.") String loginId,
    @Email(message = "이메일 형식이 아닙니다.") String email,
    @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{10,}$",
            message = "비밀번호는 영문과 숫자를 혼합하여 10자 이상이어야 합니다.")
        String password,
    @NotBlank(message = "전화번호는 필수입니다.") String phoneNumber,
    @NotBlank(message = "주소는 필수입니다.") String address,
    @NotBlank(message = "주민등록번호는 필수입니다.") String rrn,
    String drn, // 선택 항목으로 변경
    String protectorName, // 선택 항목으로 변경
    String protectorContact, // 선택 항목으로 변경
    String socialWorkerId,
    @NotNull(message = "생년월일은 필수입니다.") @JsonFormat(pattern = "yyyy/MM/dd") LocalDate birthDate,
    @NotBlank(message = "성별은 필수입니다.")
        @Pattern(regexp = "^(남|여)$", message = "성별은 '남' 또는 '여'로 입력해주세요.")
        String gender,
    @NotBlank(message = "FCM 토큰은 필수입니다.") String fcmToken // 프론트에서 자동으로 넣어줘야하는 값
    ) {}
