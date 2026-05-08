package com.kgu.life_watch.domain.health.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kgu.life_watch.domain.health.dto.request.WatchHealthDataRequest;
import com.kgu.life_watch.domain.health.entity.HealthData;
import com.kgu.life_watch.domain.health.repository.HealthDataRepository;

@Service
public class HealthDataService {

  private final HealthDataRepository healthDataRepository;

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public HealthDataService(HealthDataRepository healthDataRepository) {
    this.healthDataRepository = healthDataRepository;
  }

  @Transactional
  public void syncHealthData(WatchHealthDataRequest request) {
    Long userId = request.getUserId();

    if (userId == null) {
      throw new IllegalArgumentException("userId is required");
    }

    LocalDate currentDate = parseDate(request.getCurrentDate());
    LocalDateTime currentHeartRateTime = parseDateTimeOrNull(request.getCurrentHeartRateTime());
    LocalDateTime lastUpdatedAt = parseDateTimeOrNow(request.getLastUpdatedAt());

    if (request.getDailyRows() == null) {
      return;
    }

    for (WatchHealthDataRequest.DailyHealthRow row : request.getDailyRows()) {
      LocalDate recordDate = parseDate(row.getDate());

      HealthData healthData =
          healthDataRepository
              .findByUserIdAndRecordDate(userId, recordDate)
              .orElseGet(
                  () -> {
                    HealthData newData = new HealthData();
                    newData.setUserId(userId);
                    newData.setRecordDate(recordDate);
                    return newData;
                  });

      healthData.updateDailyData(
          row.getStepsTotal(),
          row.getHeartRateMin(),
          row.getHeartRateMax(),
          row.getHeartRateAvg(),
          row.getSleepMinutes(),
          row.getSleepHours(),
          lastUpdatedAt);

      if (recordDate.equals(currentDate)) {
        healthData.updateCurrentHeartRate(request.getCurrentHeartRate(), currentHeartRateTime);
      }

      healthDataRepository.save(healthData);
    }
  }

  private LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("날짜가 필요합니다.");
    }

    return LocalDate.parse(value);
  }

  private LocalDateTime parseDateTimeOrNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
  }

  private LocalDateTime parseDateTimeOrNow(String value) {
    if (value == null || value.isBlank()) {
      return LocalDateTime.now();
    }

    return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
  }
}
