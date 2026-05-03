package com.kgu.life_watch.domain.health.dto.request;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GalaxyWatchHealthDataRequest(
    @NotNull(message = "측정 시간은 필수입니다.")
    LocalDateTime measuredAt,

    String deviceName,

    @Min(value = 0, message = "심박수는 0 이상이어야 합니다.")
    @Max(value = 250, message = "심박수가 너무 큽니다.")
    Integer heartRate,

    @Min(value = 0, message = "걸음 수는 0 이상이어야 합니다.")
    Integer stepCount,

    @Min(value = 0, message = "혈중산소는 0 이상이어야 합니다.")
    @Max(value = 100, message = "혈중산소는 100 이하여야 합니다.")
    Integer bloodOxygen,

    @Min(value = 0, message = "총 수면 시간은 0 이상이어야 합니다.")
    Integer totalSleepMinutes,

    @Min(value = 0, message = "깨어있는 시간은 0 이상이어야 합니다.")
    Integer awakeMinutes,

    LocalDateTime sleepStartedAt,

    LocalDateTime sleepEndedAt,

    Map<String, Object> rawData
) {
}