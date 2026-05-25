package com.kgu.life_watch.domain.health.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kgu.life_watch.domain.health.dto.request.WatchHealthDataRequest;
import com.kgu.life_watch.domain.health.dto.response.HealthDataDetailResponse;
import com.kgu.life_watch.domain.health.entity.HealthData;
import com.kgu.life_watch.domain.health.repository.HealthDataRepository;

@Service
public class HealthDataService {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(HealthDataService.class);
  private final HealthDataRepository healthDataRepository;

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public HealthDataService(HealthDataRepository healthDataRepository) {
    this.healthDataRepository = healthDataRepository;
  }

  @Transactional(readOnly = true)
  public HealthDataDetailResponse getHealthDataToday(Long userId) {
    return healthDataRepository
        .findByUserIdAndRecordDate(userId, java.time.LocalDate.now())
        .map(HealthDataDetailResponse::from)
        .orElse(HealthDataDetailResponse.empty());
  }

  @Transactional(readOnly = true)
  public java.util.List<HealthDataDetailResponse> getHealthHistory(Long userId) {
    return healthDataRepository.findByUserIdOrderByRecordDateDesc(userId).stream()
        .map(HealthDataDetailResponse::from)
        .toList();
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

      if (row.getWakeMinutes() != null) {
        healthData.setWakeMinutes(row.getWakeMinutes());
      }
      if (row.getRemMinutes() != null) {
        healthData.setRemMinutes(row.getRemMinutes());
      }
      if (row.getLightMinutes() != null) {
        healthData.setLightMinutes(row.getLightMinutes());
      }
      if (row.getDeepMinutes() != null) {
        healthData.setDeepMinutes(row.getDeepMinutes());
      }

      if (recordDate.equals(currentDate)) {
        healthData.updateCurrentHeartRate(request.getCurrentHeartRate(), currentHeartRateTime);
      }

      healthDataRepository.save(healthData);

      log.info(
          "[⌚ 갤럭시워치 데이터 동기화 완료] 사용자ID: {}, 날짜: {}, 총걸음수: {}, 평균심박수: {} bpm, 수면시간: {}시간",
          userId,
          recordDate,
          row.getStepsTotal(),
          row.getHeartRateAvg(),
          row.getSleepHours());
    }
  }

  @Transactional(readOnly = true)
  public java.util.Optional<HealthData> getLatestHealthData(Long userId) {
    return healthDataRepository.findByUserIdOrderByRecordDateDesc(userId).stream().findFirst();
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

  @Transactional(readOnly = true)
  public java.util.Map<String, Object> getLifestyleInsights(Long userId) {
    java.util.List<HealthData> history =
        healthDataRepository.findByUserIdOrderByRecordDateDesc(userId);

    if (history == null || history.isEmpty()) {
      return java.util.Map.of(
          "sleepRegularity", 71,
          "outingFrequency", 22,
          "mealRegularity", 88,
          "aiBriefingText",
              "어르신의 최근 건강 이력 데이터가 충분히 쌓이지 않았습니다. 어플에 생체 데이터를 정기적으로 동기화해주시면 훌륭한 맞춤형 AI 건강 지침 리포트가 생성됩니다.");
    }

    java.util.List<java.util.Map<String, Object>> dailyRows = new java.util.ArrayList<>();
    for (HealthData hd : history) {
      java.util.Map<String, Object> row = new java.util.HashMap<>();
      row.put("date", hd.getRecordDate().toString());
      row.put("stepsTotal", hd.getStepsTotal() != null ? hd.getStepsTotal() : 0L);
      row.put("sleepHours", hd.getSleepHours() != null ? hd.getSleepHours() : 0.0);
      row.put("heartRateAvg", hd.getHeartRateAvg() != null ? hd.getHeartRateAvg() : 72);
      dailyRows.add(row);
    }

    java.util.Map<String, Object> aiRequest =
        java.util.Map.of(
            "elderlyId", userId,
            "dailyRows", dailyRows);

    try {
      org.springframework.web.client.RestTemplate restTemplate =
          new org.springframework.web.client.RestTemplate();
      String aiUrl = "http://127.0.0.1:5000/ai/analyze-lifestyle";

      @SuppressWarnings("unchecked")
      java.util.Map<String, Object> response =
          restTemplate.postForObject(aiUrl, aiRequest, java.util.Map.class);
      if (response != null) {
        return response;
      }
    } catch (Exception e) {
      log.error("❌ [AI 분석 실패] AI Flask 서버 통신 에러: ", e);
    }

    return java.util.Map.of(
        "sleepRegularity", 71,
        "outingFrequency", 22,
        "mealRegularity", 88,
        "aiBriefingText", "[임시 알림] AI 서버와의 연결이 원활하지 않습니다. 잠시 후 다시 조회를 시도해 주세요.");
  }
}
