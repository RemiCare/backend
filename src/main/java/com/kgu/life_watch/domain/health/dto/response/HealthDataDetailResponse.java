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
    Double sleepHours,
    Long wakeMinutes,
    Long remMinutes,
    Long lightMinutes,
    Long deepMinutes,
    Double activeCalories,
    Double distance,
    Integer respiratoryRate,
    Double oxygenSaturation) {
  public static HealthDataDetailResponse from(HealthData h) {
    return new HealthDataDetailResponse(
        h.getRecordDate(),
        h.getStepsTotal(),
        h.getHeartRateMin(),
        h.getHeartRateMax(),
        h.getHeartRateAvg(),
        h.getCurrentHeartRate(),
        h.getSleepMinutes(),
        h.getSleepHours(),
        h.getWakeMinutes(),
        h.getRemMinutes(),
        h.getLightMinutes(),
        h.getDeepMinutes(),
        h.getActiveCalories(),
        h.getDistance(),
        h.getRespiratoryRate(),
        h.getOxygenSaturation());
  }

  public static HealthDataDetailResponse empty() {
    return new HealthDataDetailResponse(
        LocalDate.now(), 0L, null, null, null, null, 0L, 0.0, 0L, 0L, 0L, 0L, 0.0, 0.0, null, null);
  }
}
