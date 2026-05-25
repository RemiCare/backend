package com.kgu.life_watch.domain.notification.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MedicineAlarmRequest(
    String medicineName,
    @NotBlank @Pattern(regexp = "ONCE|DAILY|EVERY_OTHER_DAY|WEEKLY") String repeatCycle,
    String medicineNote,
    @NotEmpty List<@Pattern(regexp = "^\\d{2}:\\d{2}$") String> times, // (08:00, 13:00) 이런식으로 쓰기
    @NotNull(message = "복용량은 필수입니다.") List<Double> dosage) {}
