package com.kgu.life_watch.domain.medication.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.medication.dto.request.MedicationCreateRequest;
import com.kgu.life_watch.domain.medication.dto.response.MedicationResponse;
import com.kgu.life_watch.domain.medication.service.MedicationService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/medication")
@Tag(name = "MedicationController", description = "복약 관리 관련 API")
public class MedicationController {

  private final MedicationService medicationService;

  @PostMapping
  @Operation(summary = "복약 일정 등록", description = "어르신의 새로운 복약 일정을 등록합니다.")
  public ApiResponse<Void> createMedication(@RequestBody @Valid MedicationCreateRequest request) {
    medicationService.createMedication(request);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @GetMapping("/elderly/{elderlyId}")
  @Operation(summary = "어르신별 복약 목록 조회", description = "특정 어르신의 모든 복약 일정을 조회합니다.")
  public ApiResponse<MedicationResponse> getMedications(@PathVariable Long elderlyId) {
    List<MedicationResponse> response =
        medicationService.getMedicationList(elderlyId).stream()
            .map(MedicationResponse::from)
            .toList();

    return new ApiResponse<MedicationResponse>(response);
  }

  @PatchMapping("/{medicationId}/status")
  @Operation(summary = "복약 상태 업데이트", description = "어르신이 약을 복용했는지 여부를 업데이트합니다. (보호자 수동 조작 포함)")
  public ApiResponse<Void> updateStatus(
      @PathVariable Long medicationId, @RequestParam boolean isTaken) {
    medicationService.updateMedicationStatus(medicationId, isTaken);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }
}
