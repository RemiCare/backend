package com.kgu.life_watch.domain.user.dto.response;

import java.time.LocalDate;

import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.entity.User.Role;

public record UserProfileResponse(
    Long id,
    String name,
    String loginId,
    String email,
    String phoneNumber,
    String address,
    String rrn,
    LocalDate birthDate,
    String gender,
    Role role) {
  public static UserProfileResponse toDto(User user) {
    return new UserProfileResponse(
        user.getId(),
        user.getName(),
        user.getLoginId(),
        user.getEmail(),
        user.getPhoneNumber(),
        user.getAddress(),
        user.getRrn(),
        user.getBirthDate(),
        user.getGender(),
        user.getRole());
  }
}
