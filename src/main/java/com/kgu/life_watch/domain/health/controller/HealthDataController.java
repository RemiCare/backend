package com.kgu.life_watch.domain.health.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.health.dto.request.GalaxyWatchHealthDataRequest;
import com.kgu.life_watch.domain.health.dto.response.HealthDataResponse;
import com.kgu.life_watch.domain.health.service.HealthDataService;
import com.kgu.life_watch.global.dto.response.ApiResponse;
import com.kgu.life_watch.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/health-data")
@Tag(name = "HealthDataController", description = "갤럭시 워치 생체 데이터 API")
public class HealthDataController {

  private final HealthDataService healthDataService;

  @PostMapping("/galaxy-watch")
  @Operation(summary = "갤럭시 워치 생체 데이터 저장", description = "심박수, 걸음수, 혈중산소, 수면 데이터를 저장합니다.")
  public ApiResponse<HealthDataResponse> saveGalaxyWatchHealthData(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid GalaxyWatchHealthDataRequest request
  ) {
    return new ApiResponse<>(
        healthDataService.saveGalaxyWatchData(userDetails.user().getId(), request)
    );
  }

  @GetMapping("/me/latest")
  @Operation(summary = "내 최신 생체 데이터 조회", description = "로그인한 노인의 최신 생체 데이터를 조회합니다.")
  public ApiResponse<HealthDataResponse> getMyLatestHealthData(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return new ApiResponse<>(
        healthDataService.getMyLatestHealthData(userDetails.user().getId())
    );
  }

  @GetMapping("/me")
  @Operation(summary = "내 생체 데이터 목록 조회", description = "로그인한 노인의 생체 데이터 목록을 조회합니다.")
  public ApiResponse<HealthDataResponse> getMyHealthDataList(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      LocalDateTime from,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      LocalDateTime to
  ) {
    return new ApiResponse<>(
        healthDataService.getMyHealthDataList(userDetails.user().getId(), from, to)
    );
  }

  @GetMapping("/elderly/{elderlyId}/latest")
  @Operation(summary = "담당 노인 최신 생체 데이터 조회", description = "사회복지사가 담당 노인의 최신 생체 데이터를 조회합니다.")
  public ApiResponse<HealthDataResponse> getElderlyLatestHealthData(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long elderlyId
  ) {
    return new ApiResponse<>(
        healthDataService.getElderlyLatestHealthData(userDetails.user(), elderlyId)
    );
  }
}