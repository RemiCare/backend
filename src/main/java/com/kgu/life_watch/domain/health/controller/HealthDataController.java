package com.kgu.life_watch.domain.health.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.kgu.life_watch.domain.health.dto.request.WatchHealthDataRequest;
import com.kgu.life_watch.domain.health.dto.response.HealthDataResponse;
import com.kgu.life_watch.domain.health.service.HealthDataService;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.UserRepository;
import com.kgu.life_watch.global.dto.response.ApiResponse;
import com.kgu.life_watch.global.security.CustomUserDetails;

@RestController
@RequestMapping("/api/health")
public class HealthDataController {

  private final HealthDataService healthDataService;
  private final UserRepository userRepository;

  public HealthDataController(HealthDataService healthDataService, UserRepository userRepository) {
    this.healthDataService = healthDataService;
    this.userRepository = userRepository;
  }

  @PostMapping("/sync")
  public ResponseEntity<HealthDataResponse> syncHealthData(
      @RequestBody WatchHealthDataRequest request) {
    healthDataService.syncHealthData(request);

    return ResponseEntity.ok(HealthDataResponse.success("health data sync success"));
  }

  @Transactional(readOnly = true)
  @GetMapping("/latest")
  public ApiResponse<Map<String, Object>> getLatestHealthData(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    // Detached 상태인 userDetails.user() 대신 DB에서 새로 조회하여 영속성 컨텍스트에 올림
    User user =
        userRepository
            .findById(userDetails.user().getId())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    Long targetId = user.getId();

    // 만약 로그인한 사용자가 보호자라면, 관리 중인 첫 번째 어르신의 ID를 타겟으로 설정
    if (user.getRole() == User.Role.PROTECTOR && user.getProtectorProfile() != null) {
      var seniors = user.getProtectorProfile().getAssignedSeniors();
      if (seniors != null && !seniors.isEmpty()) {
        targetId = seniors.get(0).getUser().getId();
      }
    }

    return healthDataService
        .getLatestHealthData(targetId)
        .map(
            data -> {
              Map<String, Object> map = new java.util.HashMap<>();
              map.put("heartRate", data.getCurrentHeartRate());
              map.put("steps", data.getStepsTotal());
              map.put("bloodOxygen", 98);
              map.put("timestamp", data.getLastUpdatedAt());

              // 앱의 useVitals.js가 data.results[0] 구조를 기대하므로 리스트로 감싸서 반환
              return ApiResponse.ok(java.util.List.of(map));
            })
        .orElseGet(
            () ->
                ApiResponse.ok(
                    java.util.List.of(
                        Map.of(
                            "heartRate", 72,
                            "steps", 0,
                            "bloodOxygen", 98,
                            "status", "normal"))));
  }
}
