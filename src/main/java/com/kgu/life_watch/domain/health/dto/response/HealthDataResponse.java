package com.kgu.life_watch.domain.health.dto.response;

import java.time.LocalDateTime;

import com.kgu.life_watch.domain.health.entity.HealthData;

public record HealthDataResponse(
    Long id,
    Long userId,
    LocalDateTime measuredAt,
    String deviceName,
    Integer heartRate,
    Integer stepCount,
    Integer bloodOxygen,
    Integer totalSleepMinutes,
    Integer awakeMinutes,
    String rawData
) {

  public static HealthDataResponse from(HealthData healthData) {
    return new HealthDataResponse(
        healthData.getId(),
        healthData.getUser().getId(),
        healthData.getMeasuredAt(),
        healthData.getDeviceName(),
        healthData.getHeartRate(),
        healthData.getStepCount(),
        healthData.getBloodOxygen(),
        healthData.getTotalSleepMinutes(),
        healthData.getAwakeMinutes(),
        healthData.getRawData()
    );
  }
}