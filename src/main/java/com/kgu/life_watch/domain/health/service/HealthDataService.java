package com.kgu.life_watch.domain.health.service;

// 임시 추가
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import com.kgu.life_watch.domain.alarm.dto.request.EmergencyAlarmRequest;
import com.kgu.life_watch.domain.alarm.service.AlarmService;
import com.kgu.life_watch.domain.health.dto.request.WatchHealthDataRequest;
import com.kgu.life_watch.domain.health.dto.response.HealthDataDetailResponse;
import com.kgu.life_watch.domain.health.entity.HealthData;
import com.kgu.life_watch.domain.health.repository.HealthDataRepository;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.UserRepository;

@Service
// 임시 추가
@Slf4j
public class HealthDataService {

  private final HealthDataRepository healthDataRepository;
  private final AlarmService alarmService;
  private final UserRepository userRepository;

  // 같은 위험 알림이 너무 자주 가지 않도록 막는 임시 메모리 쿨다운
  private final Map<String, LocalDateTime> lastAlarmSentAt = new ConcurrentHashMap<>();

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public HealthDataService(
      HealthDataRepository healthDataRepository,
      AlarmService alarmService,
      UserRepository userRepository) {
    this.healthDataRepository = healthDataRepository;
    this.alarmService = alarmService;
    this.userRepository = userRepository;
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

    Long todaySleepMinutes = null;

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
        todaySleepMinutes = row.getSleepMinutes();
      }

      healthDataRepository.save(healthData);
    }

    handleRiskAlarm(request, todaySleepMinutes);
  }

  @Transactional(readOnly = true)
  public java.util.Optional<HealthData> getLatestHealthData(Long userId) {
    return healthDataRepository.findByUserIdOrderByRecordDateDesc(userId).stream().findFirst();
  }

  private void handleRiskAlarm(WatchHealthDataRequest request, Long todaySleepMinutes) {
    RiskResult risk = evaluateRisk(request.getCurrentHeartRate(), todaySleepMinutes);

    log.info(
        "[HEALTH RISK] userId={}, heartRate={}, sleepMinutes={}, level={}, type={}",
        request.getUserId(),
        request.getCurrentHeartRate(),
        todaySleepMinutes,
        risk.level(),
        risk.type());

    if (!risk.isRisk()) {
      log.info("[HEALTH RISK] 정상 상태 - 알림 발송 안 함");
      return;
    }

    Long targetUserId = resolveAlarmTargetUserId(request.getUserId());

    log.info(
        "[HEALTH ALARM] targetUserId={}, sourceHealthUserId={}, grade={}, type={}",
        targetUserId,
        request.getUserId(),
        risk.grade(),
        risk.type());

    if (!canSendAlarm(targetUserId, risk)) {
      log.info(
          "[HEALTH ALARM] skipped by cooldown - userId={}, type={}", targetUserId, risk.type());
      return;
    }

    EmergencyAlarmRequest alarmRequest = new EmergencyAlarmRequest();
    alarmRequest.setUserId(targetUserId);
    alarmRequest.setLevel(risk.level());
    alarmRequest.setType(risk.type());
    alarmRequest.setTitle(risk.title());
    alarmRequest.setBody(risk.body());

    log.info(
        "[HEALTH ALARM] send attempt - userId={}, level={}, type={}",
        targetUserId,
        risk.level(),
        risk.type());

    alarmService.sendEmergencyAlarm(alarmRequest);
  }

  /** 생체 데이터의 userId가 어르신이면 보호자 userId로 변환. 생체 데이터의 userId가 보호자면 그대로 보호자에게 보냄. */
  private Long resolveAlarmTargetUserId(Long healthUserId) {
    User user =
        userRepository
            .findById(healthUserId)
            .orElseThrow(
                () -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + healthUserId));

    if (user.getRole() == User.Role.ELDER
        && user.getElderlyProfile() != null
        && user.getElderlyProfile().getProtectorProfile() != null
        && user.getElderlyProfile().getProtectorProfile().getUser() != null) {
      return user.getElderlyProfile().getProtectorProfile().getUser().getId();
    }

    return user.getId();
  }

  private RiskResult evaluateRisk(Integer currentHeartRate, Long sleepMinutes) {
    RiskResult heartRisk = evaluateHeartRateRisk(currentHeartRate);
    RiskResult sleepRisk = evaluateSleepRisk(sleepMinutes);

    return higherRisk(heartRisk, sleepRisk);
  }

  private RiskResult evaluateHeartRateRisk(Integer heartRate) {
    if (heartRate == null) {
      return RiskResult.normal();
    }

    if (heartRate >= 130) {
      return new RiskResult(
          1,
          "GRADE_1",
          "HEART_RATE_EMERGENCY",
          "🚨 1등급 긴급 알림",
          "현재 심박수 " + heartRate + "bpm이 감지되었습니다. 즉시 확인이 필요합니다.",
          2);
    }

    if (heartRate >= 110) {
      return new RiskResult(
          2,
          "GRADE_2",
          "HEART_RATE_WARNING",
          "⚠️ 2등급 주의 알림",
          "현재 심박수 " + heartRate + "bpm입니다. 어르신 상태 확인이 필요합니다.",
          30);
    }

    if (heartRate >= 100) {
      return new RiskResult(
          3,
          "GRADE_3",
          "HEART_RATE_NOTICE",
          "심박수 상승 알림",
          "현재 심박수 " + heartRate + "bpm입니다. 평소보다 높을 수 있습니다.",
          60);
    }

    return RiskResult.normal();
  }

  private RiskResult evaluateSleepRisk(Long sleepMinutes) {
    // null 또는 0 이하는 수면 데이터 없음으로 보고 판단하지 않음
    if (sleepMinutes == null || sleepMinutes <= 0) {
      return RiskResult.normal();
    }

    if (sleepMinutes < 180) {
      return new RiskResult(
          2,
          "GRADE_2",
          "SLEEP_WARNING",
          "⚠️ 수면 부족 주의 알림",
          "총 수면시간이 3시간 미만입니다. 어르신 상태 확인이 필요합니다.",
          30);
    }

    if (sleepMinutes < 300) {
      return new RiskResult(
          3, "GRADE_3", "SLEEP_NOTICE", "수면 부족 알림", "총 수면시간이 5시간 미만입니다. 컨디션 확인이 필요합니다.", 60);
    }

    return RiskResult.normal();
  }

  private RiskResult higherRisk(RiskResult a, RiskResult b) {
    if (!a.isRisk()) {
      return b;
    }

    if (!b.isRisk()) {
      return a;
    }

    // 숫자가 작을수록 더 위험함. 1등급 > 2등급 > 3등급
    return a.grade() <= b.grade() ? a : b;
  }

  private boolean canSendAlarm(Long targetUserId, RiskResult risk) {
    String key = targetUserId + ":" + risk.type();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime lastSentAt = lastAlarmSentAt.get(key);

    if (lastSentAt != null && lastSentAt.plusMinutes(risk.cooldownMinutes()).isAfter(now)) {
      return false;
    }

    lastAlarmSentAt.put(key, now);
    return true;
  }

  private record RiskResult(
      int grade, String level, String type, String title, String body, int cooldownMinutes) {
    static RiskResult normal() {
      return new RiskResult(0, "NORMAL", "NORMAL", null, null, 0);
    }

    boolean isRisk() {
      return grade > 0;
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
