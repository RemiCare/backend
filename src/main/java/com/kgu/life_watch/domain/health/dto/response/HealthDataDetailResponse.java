package com.kgu.life_watch.domain.health.dto.response;

import java.time.LocalDate;

import com.kgu.life_watch.domain.health.entity.HealthData;

public record HealthDataDetailResponse(
    LocalDate recordDate,
    Long stepsTotal,
    Integer heartRateMin,
    Integer heartRateMax,
    Integer heartRateAvg,
    Integer currentHeartRate,
    Long sleepMinutes,
    Double sleepHours) {
  public static HealthDataDetailResponse from(HealthData h) {
    return new HealthDataDetailResponse(
        h.getRecordDate(),
        h.getStepsTotal(),
        h.getHeartRateMin(),
        h.getHeartRateMax(),
        h.getHeartRateAvg(),
        h.getCurrentHeartRate(),
        h.getSleepMinutes(),
        h.getSleepHours());
  }

  public static HealthDataDetailResponse empty() {
    return new HealthDataDetailResponse(LocalDate.now(), 0L, null, null, null, null, 0L, 0.0);
  }
}
