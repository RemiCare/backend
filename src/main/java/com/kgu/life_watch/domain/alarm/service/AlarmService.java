package com.kgu.life_watch.domain.alarm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kgu.life_watch.domain.alarm.dto.request.EmergencyAlarmRequest;
import com.kgu.life_watch.domain.alarm.dto.request.PushTokenRegisterRequest;
import com.kgu.life_watch.domain.alarm.entity.AlarmPushToken;
import com.kgu.life_watch.domain.alarm.repository.AlarmPushTokenRepository;
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
  }

  public String sendEmergencyAlarm(EmergencyAlarmRequest request) {
    if (request.getUserId() == null) {
      throw new IllegalArgumentException("userId is required");
    }

    List<AlarmPushToken> tokens =
        alarmPushTokenRepository.findByUserIdAndEnabledTrue(request.getUserId());

    if (tokens.isEmpty()) {
      return "No push token registered for userId=" + request.getUserId();
    }

    List<Map<String, Object>> messages = new ArrayList<>();

    for (AlarmPushToken token : tokens) {
      messages.add(buildExpoMessage(token.getExpoPushToken(), request));
    }

    return sendToExpo(messages);
  }

  private Map<String, Object> buildExpoMessage(String expoPushToken, EmergencyAlarmRequest request) {
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

    Map<String, Object> message = new HashMap<>();
    message.put("to", expoPushToken);
    message.put("sound", "default");
    message.put("title", title);
    message.put("body", body);
    message.put("data", data);

    return message;
  }

  private String makeDefaultTitle(String level) {
    if ("HIGH".equalsIgnoreCase(level) || "high".equalsIgnoreCase(level)) {
      return "🚨 고위험 응급 알림";
    }

    if ("CAUTION".equalsIgnoreCase(level)
        || "WARNING".equalsIgnoreCase(level)
        || "mid".equalsIgnoreCase(level)) {
      return "⚠️ 주의 건강 알림";
    }

    return "건강 상태 알림";
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

      return "Expo response code="
          + response.statusCode()
          + ", body="
          + response.body();

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