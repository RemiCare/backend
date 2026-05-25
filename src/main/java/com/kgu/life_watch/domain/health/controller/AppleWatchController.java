package com.kgu.life_watch.domain.health.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.kgu.life_watch.domain.health.entity.HealthData;
import com.kgu.life_watch.domain.health.repository.HealthDataRepository;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/apple")
public class AppleWatchController {

  private final HealthDataRepository healthDataRepository;

  @GetMapping("/status")
  public ResponseEntity<?> getStatus() {
    log.info("⌚ [Apple Watch API] /status GET 요청 수신됨");
    return ResponseEntity.ok(Map.of("status", "ok", "message", "Apple Watch API is ready"));
  }

  @SuppressWarnings("unchecked")
  @PostMapping("/batch")
  public ResponseEntity<?> receiveBatchData(
      @RequestParam(value = "userId", required = false) Long userId,
      @RequestBody Object rawPayload) {

    log.info("⌚ [Apple Watch API] /batch POST 요청 수신됨!");

    // 1. 디폴트 어르신 ID 설정 (디폴트: 2L)
    if (userId == null) {
      userId = 2L;
    }

    // 디버깅 용도로 수신 받은 껍데기를 리소스 폴더에 로컬 저장
    try {
      String filePath =
          "C:\\Users\\LG\\Downloads\\backend-feature-alarm\\backend-feature-alarm\\src\\main\\resources\\apple_raw_payload.json";
      com.fasterxml.jackson.databind.ObjectMapper mapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      mapper.writerWithDefaultPrettyPrinter().writeValue(new java.io.File(filePath), rawPayload);
    } catch (Exception ignored) {
    }

    try {
      // [케이스 A] 최상위가 JSON 배열 [ ... ] 형식으로 들어오는 경우 (List)
      if (rawPayload instanceof List) {
        List<Map<String, Object>> samples = (List<Map<String, Object>>) rawPayload;
        log.info("📊 [Apple Watch API] 최상위 List 배열 페이로드 감지 (샘플 수: {})", samples.size());

        for (Map<String, Object> sample : samples) {
          String metric = (String) sample.get("metric");
          String dateStr = (String) sample.get("date");
          Object valObj = sample.get("value");

          if (metric == null || dateStr == null || valObj == null) continue;

          Number valueNum =
              (valObj instanceof Number) ? (Number) valObj : Double.parseDouble(valObj.toString());

          // ISO-8601 파싱 및 KST 변환
          ZonedDateTime utcDateTime = ZonedDateTime.parse(dateStr);
          ZonedDateTime kstDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
          LocalDate recordDate = kstDateTime.toLocalDate();
          LocalDateTime recordDateTime = kstDateTime.toLocalDateTime();

          // DB에서 해당 어르신 & 날짜 레코드 조회 (없으면 새로 생성)
          Optional<HealthData> existingData =
              healthDataRepository.findByUserIdAndRecordDate(userId, recordDate);
          HealthData healthData;
          if (existingData.isPresent()) {
            healthData = existingData.get();
          } else {
            healthData = new HealthData();
            healthData.setUserId(userId);
            healthData.setRecordDate(recordDate);
            healthData.setStepsTotal(0L);
            healthData.setHeartRateMin(999);
            healthData.setHeartRateMax(0);
            healthData.setHeartRateAvg(72);
          }

          // 6대 데이터 종류 정밀 매칭 및 동기화 로그
          if ("step_count".equalsIgnoreCase(metric)) {
            healthData.setStepsTotal(valueNum.longValue());
            healthData.setLastUpdatedAt(LocalDateTime.now());
            log.info("🚶 [걸음수 동기화 성공] 날짜: {}, 걸음수: {}", recordDate, valueNum.longValue());
          } else if ("heart_rate".equalsIgnoreCase(metric)) {
            int hrValue = valueNum.intValue();
            healthData.setCurrentHeartRate(hrValue);
            healthData.setCurrentHeartRateTime(recordDateTime);

            int minHr =
                healthData.getHeartRateMin() == null || healthData.getHeartRateMin() == 999
                    ? hrValue
                    : Math.min(healthData.getHeartRateMin(), hrValue);
            int maxHr =
                healthData.getHeartRateMax() == null
                    ? hrValue
                    : Math.max(healthData.getHeartRateMax(), hrValue);
            int avgHr =
                healthData.getHeartRateAvg() == null
                    ? hrValue
                    : (healthData.getHeartRateAvg() + hrValue) / 2;

            healthData.setHeartRateMin(minHr);
            healthData.setHeartRateMax(maxHr);
            healthData.setHeartRateAvg(avgHr);
            healthData.setLastUpdatedAt(LocalDateTime.now());
            log.info("💓 [심박수 동기화 성공] 날짜: {}, 심박수: {} bpm", recordDate, hrValue);
          } else if ("sleep_analysis".equalsIgnoreCase(metric)
              || "sleep_duration".equalsIgnoreCase(metric)
              || "sleep".equalsIgnoreCase(metric)) {
            double hours = valueNum.doubleValue();
            long minutes = Math.round(hours * 60);

            long prevMin = healthData.getSleepMinutes() != null ? healthData.getSleepMinutes() : 0L;
            healthData.setSleepMinutes(prevMin + minutes);
            healthData.setSleepHours((prevMin + minutes) / 60.0);

            String sleepType = null;
            if (sample.containsKey("sleep_type")) sleepType = (String) sample.get("sleep_type");
            else if (sample.containsKey("sleepType")) sleepType = (String) sample.get("sleepType");
            else if (sample.containsKey("stage")) sleepType = (String) sample.get("stage");

            if (sleepType != null) {
              com.kgu.life_watch.domain.health.entity.SleepStage stage =
                  com.kgu.life_watch.domain.health.service.SleepStageMapper.fromAppleSleepValue(
                      sleepType);
              healthData.addSleepStageMinutes(stage, minutes);
              log.info(
                  "😴 [수면 세부단계 누적] 날짜: {}, 단계: {} (원문: {}), 누적시간: {} 분",
                  recordDate,
                  stage,
                  sleepType,
                  minutes);
            } else {
              healthData.addSleepStageMinutes(
                  com.kgu.life_watch.domain.health.entity.SleepStage.LIGHT, minutes);
            }

            healthData.setLastUpdatedAt(LocalDateTime.now());
            log.info(
                "😴 [수면단계 동기화 성공] 날짜: {}, 수면: {} 시간 ({} 분)",
                recordDate,
                healthData.getSleepHours(),
                healthData.getSleepMinutes());
          } else if ("oxygen_saturation".equalsIgnoreCase(metric)
              || "blood_oxygen".equalsIgnoreCase(metric)) {
            log.info(
                "◎ [혈중산소 동기화 성공] 날짜: {}, 산소농도: {} %",
                recordDate,
                (valueNum.doubleValue() < 1.0
                    ? valueNum.doubleValue() * 100
                    : valueNum.doubleValue()));
          } else if ("activeEnergyBurned".equalsIgnoreCase(metric)
              || "active_energy".equalsIgnoreCase(metric)
              || "active_calories".equalsIgnoreCase(metric)
              || "activeEnergyBurnedGoal".equalsIgnoreCase(metric)) {
            double cals = valueNum.doubleValue();
            double prevCals =
                healthData.getActiveCalories() != null ? healthData.getActiveCalories() : 0.0;
            healthData.setActiveCalories(prevCals + cals);
            healthData.setLastUpdatedAt(LocalDateTime.now());
            log.info(
                "🔥 [활동칼로리 동기화 성공] 날짜: {}, 소모량: {} kcal",
                recordDate,
                healthData.getActiveCalories());
          } else if ("respiratory_rate".equalsIgnoreCase(metric)
              || "breath_rate".equalsIgnoreCase(metric)) {
            healthData.setRespiratoryRate(valueNum.intValue());
            healthData.setLastUpdatedAt(LocalDateTime.now());
            log.info("🫁 [호흡수 동기화 성공] 날짜: {}, 호흡수: {} 회/분", recordDate, valueNum.intValue());
          } else if ("distance_walking_running".equalsIgnoreCase(metric)
              || "distance".equalsIgnoreCase(metric)) {
            double dist = valueNum.doubleValue();
            double prevDist = healthData.getDistance() != null ? healthData.getDistance() : 0.0;
            healthData.setDistance(prevDist + dist);
            healthData.setLastUpdatedAt(LocalDateTime.now());
            log.info("🛣️ [보행거리 동기화 성공] 날짜: {}, 거리: {} m", recordDate, healthData.getDistance());
          }

          healthDataRepository.save(healthData);
        }
      }
      // [케이스 B] 최상위가 JSON 객체 { metric: ..., samples: [...] } 형식으로 들어오는 경우 (Map)
      else if (rawPayload instanceof Map) {
        Map<String, Object> payload = (Map<String, Object>) rawPayload;
        String metric = (String) payload.get("metric");
        List<Map<String, Object>> samples = (List<Map<String, Object>>) payload.get("samples");

        if (metric != null && samples != null && !samples.isEmpty()) {
          log.info(
              "📊 [Apple Watch API] 최상위 Map 객체 페이로드 감지 (메트릭: {}, 샘플 수: {})",
              metric,
              samples.size());

          for (Map<String, Object> sample : samples) {
            String dateStr = (String) sample.get("date");
            if (dateStr == null) continue;

            ZonedDateTime utcDateTime = ZonedDateTime.parse(dateStr);
            ZonedDateTime kstDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
            LocalDate recordDate = kstDateTime.toLocalDate();
            LocalDateTime recordDateTime = kstDateTime.toLocalDateTime();

            Optional<HealthData> existingData =
                healthDataRepository.findByUserIdAndRecordDate(userId, recordDate);
            HealthData healthData;
            if (existingData.isPresent()) {
              healthData = existingData.get();
            } else {
              healthData = new HealthData();
              healthData.setUserId(userId);
              healthData.setRecordDate(recordDate);
              healthData.setStepsTotal(0L);
              healthData.setHeartRateMin(999);
              healthData.setHeartRateMax(0);
              healthData.setHeartRateAvg(72);
            }

            if ("step_count".equalsIgnoreCase(metric)) {
              Number qty = (Number) sample.get("qty");
              if (qty != null) {
                healthData.setStepsTotal(qty.longValue());
                healthData.setLastUpdatedAt(LocalDateTime.now());
                log.info("🚶 [걸음수 동기화 성공] 날짜: {}, 걸음수: {}", recordDate, qty.longValue());
              }
            } else if ("heart_rate".equalsIgnoreCase(metric)) {
              Number qty = (Number) sample.get("qty");
              if (qty != null) {
                int hrValue = qty.intValue();
                healthData.setCurrentHeartRate(hrValue);
                healthData.setCurrentHeartRateTime(recordDateTime);

                int minHr =
                    healthData.getHeartRateMin() == null || healthData.getHeartRateMin() == 999
                        ? hrValue
                        : Math.min(healthData.getHeartRateMin(), hrValue);
                int maxHr =
                    healthData.getHeartRateMax() == null
                        ? hrValue
                        : Math.max(healthData.getHeartRateMax(), hrValue);
                int avgHr =
                    healthData.getHeartRateAvg() == null
                        ? hrValue
                        : (healthData.getHeartRateAvg() + hrValue) / 2;

                healthData.setHeartRateMin(minHr);
                healthData.setHeartRateMax(maxHr);
                healthData.setHeartRateAvg(avgHr);
                healthData.setLastUpdatedAt(LocalDateTime.now());
                log.info("💓 [심박수 동기화 성공] 날짜: {}, 심박수: {} bpm", recordDate, hrValue);
              }
            } else if ("sleep_analysis".equalsIgnoreCase(metric)
                || "sleep_duration".equalsIgnoreCase(metric)
                || "sleep".equalsIgnoreCase(metric)) {
              Number qty = (Number) sample.get("qty");
              if (qty != null) {
                double hours = qty.doubleValue();
                long minutes = Math.round(hours * 60);

                long prevMin =
                    healthData.getSleepMinutes() != null ? healthData.getSleepMinutes() : 0L;
                healthData.setSleepMinutes(prevMin + minutes);
                healthData.setSleepHours((prevMin + minutes) / 60.0);

                String sleepType = null;
                if (sample.containsKey("sleep_type")) sleepType = (String) sample.get("sleep_type");
                else if (sample.containsKey("sleepType"))
                  sleepType = (String) sample.get("sleepType");
                else if (sample.containsKey("stage")) sleepType = (String) sample.get("stage");

                if (sleepType != null) {
                  com.kgu.life_watch.domain.health.entity.SleepStage stage =
                      com.kgu.life_watch.domain.health.service.SleepStageMapper.fromAppleSleepValue(
                          sleepType);
                  healthData.addSleepStageMinutes(stage, minutes);
                  log.info(
                      "😴 [수면 세부단계 누적] 날짜: {}, 단계: {} (원문: {}), 누적시간: {} 분",
                      recordDate,
                      stage,
                      sleepType,
                      minutes);
                } else {
                  healthData.addSleepStageMinutes(
                      com.kgu.life_watch.domain.health.entity.SleepStage.LIGHT, minutes);
                }

                healthData.setLastUpdatedAt(LocalDateTime.now());
                log.info(
                    "😴 [수면단계 동기화 성공] 날짜: {}, 수면: {} 시간 ({} 분)",
                    recordDate,
                    healthData.getSleepHours(),
                    healthData.getSleepMinutes());
              }
            } else if ("oxygen_saturation".equalsIgnoreCase(metric)
                || "blood_oxygen".equalsIgnoreCase(metric)) {
              Number qty = (Number) sample.get("qty");
              if (qty != null) {
                log.info(
                    "◎ [혈중산소 동기화 성공] 날짜: {}, 산소농도: {} %",
                    recordDate,
                    (qty.doubleValue() < 1.0 ? qty.doubleValue() * 100 : qty.doubleValue()));
              }
            } else if ("activeEnergyBurned".equalsIgnoreCase(metric)
                || "active_energy".equalsIgnoreCase(metric)
                || "active_calories".equalsIgnoreCase(metric)
                || "activeEnergyBurnedGoal".equalsIgnoreCase(metric)) {
              Number qty = (Number) sample.get("qty");
              if (qty != null) {
                double cals = qty.doubleValue();
                double prevCals =
                    healthData.getActiveCalories() != null ? healthData.getActiveCalories() : 0.0;
                healthData.setActiveCalories(prevCals + cals);
                healthData.setLastUpdatedAt(LocalDateTime.now());
                log.info(
                    "🔥 [활동칼로리 동기화 성공] 날짜: {}, 소모량: {} kcal",
                    recordDate,
                    healthData.getActiveCalories());
              }
            } else if ("respiratory_rate".equalsIgnoreCase(metric)
                || "breath_rate".equalsIgnoreCase(metric)) {
              Number qty = (Number) sample.get("qty");
              if (qty != null) {
                healthData.setRespiratoryRate(qty.intValue());
                healthData.setLastUpdatedAt(LocalDateTime.now());
                log.info("🫁 [호흡수 동기화 성공] 날짜: {}, 호흡수: {} 회/분", recordDate, qty.intValue());
              }
            } else if ("distance_walking_running".equalsIgnoreCase(metric)
                || "distance".equalsIgnoreCase(metric)) {
              Number qty = (Number) sample.get("qty");
              if (qty != null) {
                double dist = qty.doubleValue();
                double prevDist = healthData.getDistance() != null ? healthData.getDistance() : 0.0;
                healthData.setDistance(prevDist + dist);
                healthData.setLastUpdatedAt(LocalDateTime.now());
                log.info(
                    "🛣️ [보행거리 동기화 성공] 날짜: {}, 거리: {} m", recordDate, healthData.getDistance());
              }
            }

            healthDataRepository.save(healthData);
          }
        }
      }
    } catch (Exception e) {
      log.error("❌ [Apple Watch API] 데이터 저장/파싱 에러: ", e);
      return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
    }

    return ResponseEntity.ok(Map.of("status", "success", "received", true));
  }
}
