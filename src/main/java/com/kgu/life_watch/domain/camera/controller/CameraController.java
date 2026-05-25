package com.kgu.life_watch.domain.camera.controller;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.camera.dto.request.CameraZoneRequest;
import com.kgu.life_watch.domain.camera.dto.response.CameraZoneResponse;
import com.kgu.life_watch.domain.camera.service.CameraConfigService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/camera")
@RequiredArgsConstructor
@Tag(name = "CameraController", description = "AI 카메라 구역 설정 관련 API")
public class CameraController {

  private final CameraConfigService cameraConfigService;

  @PostMapping("/zone")
  @Operation(summary = "카메라 감지 구역 저장 API", description = "앱에서 설정한 B구역, C구역 좌표 비율을 저장합니다.")
  public ApiResponse<Void> saveZones(@Valid @RequestBody CameraZoneRequest request) {
    cameraConfigService.saveOrUpdateZones(request);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @GetMapping("/zone/{elderlyId}")
  @Operation(
      summary = "카메라 감지 구역 조회 API",
      description = "특정 어르신의 카메라 감지 구역 좌표 비율을 조회합니다. (AI 엔진 사용)")
  public ApiResponse<CameraZoneResponse> getZones(@PathVariable Long elderlyId) {
    CameraZoneResponse response = cameraConfigService.getZones(elderlyId);
    if (response == null) {
      return new ApiResponse<>(SuccessCode.REQUEST_OK);
    }
    return new ApiResponse<>(response);
  }
}
