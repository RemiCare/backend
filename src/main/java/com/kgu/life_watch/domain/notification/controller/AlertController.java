package com.kgu.life_watch.domain.notification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.notification.dto.EmergencyAlertRequest;
import com.kgu.life_watch.domain.notification.entity.NotificationLog;
import com.kgu.life_watch.domain.notification.repository.NotificationLogRepository;
import com.kgu.life_watch.domain.notification.service.FirebaseMessageService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
@Tag(name = "AlertController", description = "건강 관련 경고 알람 관련 API")
public class AlertController {

  private final FirebaseMessageService firebaseMessageService;
  private final NotificationLogRepository notificationLogRepository;

  @PostMapping("/emergency")
  @Operation(
      summary = "노인의 이상건강에 관한 경고 알람 API",
      description = "노인의 이상건강에 관한 경고 알람을 담당 사회복지사에게 전달하는 API입니다.")
  public ApiResponse<Void> handleEmergency(@Valid @RequestBody EmergencyAlertRequest request) {
    firebaseMessageService.sendEmergencyAlert(
        request.elderlyId(), request.predictionLabel(), request.explanation());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @GetMapping("/{elderlyId}")
  @Operation(summary = "알람 확인 API", description = "발생한 알람을 확인하게 해주는 API입니다.")
  public ApiResponse<NotificationLog> getNotifications(@PathVariable Long elderlyId) {
    List<NotificationLog> logs =
        notificationLogRepository.findByElderlyIdOrderBySentAtDesc(elderlyId);
    return new ApiResponse<>(logs);
  }
}
