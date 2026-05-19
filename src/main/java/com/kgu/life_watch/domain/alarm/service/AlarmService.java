package com.kgu.life_watch.domain.alarm.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import com.kgu.life_watch.domain.alarm.dto.request.EmergencyAlarmRequest;
import com.kgu.life_watch.domain.alarm.dto.request.PushTokenRegisterRequest;
import com.kgu.life_watch.domain.alarm.entity.AlarmPushToken;
import com.kgu.life_watch.domain.alarm.repository.AlarmPushTokenRepository;

@Slf4j
@Service
public class AlarmService {

  private static final String EXPO_PUSH_API_URL = "https://exp.host/--/api/v2/push/send";

  private final AlarmPushTokenRepository alarmPushTokenRepository;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public AlarmService(
      AlarmPushTokenRepository alarmPushTokenRepository, ObjectMapper objectMapper) {
    this.alarmPushTokenRepository = alarmPushTokenRepository;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newHttpClient();
  }

  @Transactional
  public void registerPushToken(PushTokenRegisterRequest request) {
    if (request.getUserId() == null) {
      throw new IllegalArgumentException("userId is required");
    }

    if (request.getExpoPushToken() == null || request.getExpoPushToken().isBlank()) {
      throw new IllegalArgumentException("expoPushToken is required");
    }

    validateExpoPushToken(request.getExpoPushToken());

    AlarmPushToken token =
        alarmPushTokenRepository
            .findByExpoPushToken(request.getExpoPushToken())
            .orElseGet(
                () -> {
                  AlarmPushToken newToken = new AlarmPushToken();
                  newToken.setExpoPushToken(request.getExpoPushToken());
                  return newToken;
                });

    token.updateTokenInfo(request.getUserId(), request.getPlatform(), request.getDeviceName());

    alarmPushTokenRepository.save(token);

    log.info(
        "[ALARM] push token registered - userId={}, platform={}, deviceName={}",
        request.getUserId(),
        request.getPlatform(),
        request.getDeviceName());
  }

  public String sendEmergencyAlarm(EmergencyAlarmRequest request) {
    if (request.getUserId() == null) {
      throw new IllegalArgumentException("userId is required");
    }

    List<AlarmPushToken> tokens =
        alarmPushTokenRepository.findByUserIdAndEnabledTrue(request.getUserId());

    if (tokens.isEmpty()) {
      log.info("[ALARM] no push token - userId={}", request.getUserId());
      return "No push token registered for userId=" + request.getUserId();
    }

    log.info(
        "[ALARM] push token found - userId={}, tokenCount={}, level={}, type={}",
        request.getUserId(),
        tokens.size(),
        request.getLevel(),
        request.getType());

    List<Map<String, Object>> messages = new ArrayList<>();

    for (AlarmPushToken token : tokens) {
      messages.add(buildExpoMessage(token.getExpoPushToken(), request));
    }

    String result = sendToExpo(messages);

    log.info("[ALARM] expo push result={}", result);

    return result;
  }

  private Map<String, Object> buildExpoMessage(
      String expoPushToken, EmergencyAlarmRequest request) {

    int grade = resolveGrade(request.getLevel());
    String channelId = makeChannelId(grade);

    String title = request.getTitle();

    if (title == null || title.isBlank()) {
      title = makeDefaultTitle(request.getLevel());
    }

    String body = request.getBody();

    if (body == null || body.isBlank()) {
      body = "AI 응급 브리핑에서 위험 상태가 감지되었습니다.";
    }

    Map<String, Object> data = new HashMap<>();
    data.put("screen", "insights");
    data.put("level", request.getLevel());
    data.put("type", request.getType());

    // 프론트 앱에서 알림 정책 처리할 때 쓰는 값
    data.put("grade", grade);
    data.put("channelId", channelId);
    data.put("sticky", grade == 1 || grade == 2);
    data.put("repeatIntervalMinutes", grade == 1 ? 2 : 0);

    Map<String, Object> message = new HashMap<>();
    message.put("to", expoPushToken);
    message.put("sound", "default");
    message.put("title", title);
    message.put("body", body);
    message.put("data", data);

    // Android 알림 채널. 앱 쪽에서도 같은 channelId를 만들어야 효과가 있음.
    message.put("channelId", channelId);

    // 1, 2등급은 우선순위 높게
    message.put("priority", grade == 1 || grade == 2 ? "high" : "default");

    return message;
  }

  private int resolveGrade(String level) {
    if (level == null || level.isBlank()) {
      return 3;
    }

    String normalized = level.toUpperCase();

    if ("GRADE_1".equals(normalized)
        || "HIGH".equals(normalized)
        || "EMERGENCY".equals(normalized)) {
      return 1;
    }

    if ("GRADE_2".equals(normalized)
        || "CAUTION".equals(normalized)
        || "WARNING".equals(normalized)
        || "MID".equals(normalized)) {
      return 2;
    }

    if ("GRADE_3".equals(normalized) || "NOTICE".equals(normalized) || "LOW".equals(normalized)) {
      return 3;
    }

    return 3;
  }

  private String makeChannelId(int grade) {
    if (grade == 1) {
      return "emergency-grade-1";
    }

    if (grade == 2) {
      return "warning-grade-2";
    }

    return "notice-grade-3";
  }

  private String makeDefaultTitle(String level) {
    int grade = resolveGrade(level);

    if (grade == 1) {
      return "🚨 1등급 긴급 알림";
    }

    if (grade == 2) {
      return "⚠️ 2등급 주의 알림";
    }

    return "3등급 건강 알림";
  }

  private String sendToExpo(List<Map<String, Object>> messages) {
    try {
      String jsonBody = objectMapper.writeValueAsString(messages);

      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(EXPO_PUSH_API_URL))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      HttpResponse<String> response =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      return "Expo response code=" + response.statusCode() + ", body=" + response.body();

    } catch (Exception e) {
      throw new RuntimeException("Failed to send Expo push notification", e);
    }
  }

  private void validateExpoPushToken(String token) {
    boolean valid = token.startsWith("ExponentPushToken[") || token.startsWith("ExpoPushToken[");

    if (!valid) {
      throw new IllegalArgumentException("Invalid Expo push token format");
    }
  }
}
