package com.kgu.life_watch.domain.alarm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kgu.life_watch.domain.alarm.dto.request.EmergencyAlarmRequest;
import com.kgu.life_watch.domain.alarm.dto.request.PushTokenRegisterRequest;
import com.kgu.life_watch.domain.alarm.dto.response.AlarmResponse;
import com.kgu.life_watch.domain.alarm.service.AlarmService;

@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

  private final AlarmService alarmService;

  public AlarmController(AlarmService alarmService) {
    this.alarmService = alarmService;
  }

  @PostMapping("/push-token/register")
  public ResponseEntity<AlarmResponse> registerPushToken(
      @RequestBody PushTokenRegisterRequest request) {
    alarmService.registerPushToken(request);

    return ResponseEntity.ok(AlarmResponse.success("push token registered"));
  }

  @PostMapping("/emergency/test")
  public ResponseEntity<AlarmResponse> sendEmergencyAlarm(
      @RequestBody EmergencyAlarmRequest request) {
    String result = alarmService.sendEmergencyAlarm(request);

    return ResponseEntity.ok(AlarmResponse.success(result));
  }
}
