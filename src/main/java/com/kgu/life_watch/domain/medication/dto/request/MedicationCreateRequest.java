package com.kgu.life_watch.domain.medication.dto.request;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicationCreateRequest(
    @Schema(description = "약 이름", example = "혈압약(아밀로디핀)") @NotBlank(message = "약 이름은 필수입니다.")
        String name,
    @Schema(description = "복용 시간", example = "09:00:00") @NotNull(message = "복용 시간은 필수입니다.")
        LocalTime dosageTime,
    @Schema(
            description = "복용 요일 (예: [MONDAY, WEDNESDAY, FRIDAY])",
            example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]")
        @NotNull(message = "복용 요일은 필수입니다.")
        List<DayOfWeek> daysOfWeek,
    @Schema(description = "대상 노인 ID", example = "1") @NotNull(message = "대상 노인 ID는 필수입니다.")
        Long elderlyId) {}
