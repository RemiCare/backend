package com.kgu.life_watch.domain.user.dto.response;

import java.time.LocalDate;

import com.kgu.life_watch.domain.user.entity.User;

public record ElderlySimpleInfoResponse(
    Long id, String name, String phoneNumber, String address, LocalDate birthDate, String gender) {
  public static ElderlySimpleInfoResponse from(User user) {
    return new ElderlySimpleInfoResponse(
        user.getId(),
        user.getName(),
        user.getPhoneNumber(),
        user.getAddress(),
        user.getBirthDate(),
        user.getGender());
  }
}
