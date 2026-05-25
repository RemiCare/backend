package com.kgu.life_watch.domain.schedule.dto.request;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScheduleCreateRequest(
    @Schema(description = "일정 제목", example = "기상 및 아침 산책") @NotBlank(message = "제목은 필수입니다.")
        String title,
    @Schema(description = "상세 내용", example = "공원 한 바퀴 돌기") String content,
    @Schema(description = "실행 시간", example = "08:00:00") @NotNull(message = "실행 시간은 필수입니다.")
        LocalTime executionTime,
    @Schema(description = "반복 요일", example = "[\"MONDAY\", \"WEDNESDAY\"]")
        @NotNull(message = "요일 설정은 필수입니다.")
        List<DayOfWeek> daysOfWeek,
    @Schema(description = "대상 노인 ID", example = "1") @NotNull(message = "대상 노인 ID는 필수입니다.")
        Long elderlyId) {}
