package com.kgu.life_watch.domain.notification.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.notification.dto.AlarmGroupDto;
import com.kgu.life_watch.domain.notification.dto.MedicineAlarmRequest;
import com.kgu.life_watch.domain.notification.service.AlarmGroupService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;
import com.kgu.life_watch.global.security.CustomUserDetails;

@RestController
@RequestMapping("/api/alarm/group")
@RequiredArgsConstructor
@Tag(name = "AlarmGroupController", description = "복용 약 알람 그룹 API")
public class AlarmGroupController {
  private final AlarmGroupService alarmGroupService;

  @PostMapping
  @Operation(summary = "복용 약 그룹 생성", description = "약 그룹을 생성하고 알람을 등록합니다.")
  public ApiResponse<Void> createAlarmGroup(
      @RequestBody @Valid MedicineAlarmRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    alarmGroupService.createAlarmGroup(request, userDetails.user());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @GetMapping
  @Operation(summary = "복용 약 그룹 목록 조회", description = "등록된 약 그룹과 알람을 조회합니다.")
  public ApiResponse<AlarmGroupDto> getGroups(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return new ApiResponse<>(alarmGroupService.getAlarmGroups(userDetails.user()));
  }

  @DeleteMapping("/{groupId}")
  @Operation(summary = "복용 약 그룹 삭제", description = "약 그룹과 알람을 삭제합니다.")
  public ApiResponse<Void> deleteGroup(@PathVariable Long groupId) {
    alarmGroupService.deleteGroup(groupId);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PatchMapping("/{groupId}")
  @Operation(summary = "복용 약 그룹 수정", description = "약 그룹의 이름과 주의사항을 수정합니다.")
  public ApiResponse<Void> updateGroup(
      @PathVariable Long groupId,
      @RequestParam @NotBlank String newName,
      @RequestParam String newNote) {
    alarmGroupService.updateGroup(groupId, newName, newNote);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PostMapping("/{groupId}/alarm")
  @Operation(summary = "복용 알람 추가", description = "그룹에 복용 알람을 추가합니다.")
  public ApiResponse<Void> addAlarmToGroup(
      @PathVariable Long groupId,
      @RequestBody @Valid MedicineAlarmRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    alarmGroupService.addAlarmToGroup(groupId, request, userDetails.user());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PutMapping("/{groupId}/alarm")
  @Operation(summary = "복용 알람 수정", description = "복용 알람의 시간과 주기를 수정합니다.")
  public ApiResponse<Void> rescheduleGroup(
      @PathVariable Long groupId,
      @RequestBody @Valid MedicineAlarmRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    alarmGroupService.rescheduleGroup(groupId, request, userDetails.user());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PatchMapping("/{groupId}/complete")
  @Operation(summary = "복용 완료 처리 (그룹)", description = "그룹 내 알람을 복용 완료로 처리합니다.")
  public ApiResponse<Void> completeAllInGroup(@PathVariable Long groupId) {
    alarmGroupService.markAllAlarmsInGroupComplete(groupId);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PatchMapping("/alarm/{alarmId}/complete")
  @Operation(summary = "복용 완료 처리 (개별)", description = "개별 알람을 복용 완료로 처리합니다.")
  public ApiResponse<Void> completeSingleAlarm(@PathVariable Long alarmId) {
    alarmGroupService.markAlarmAsCompleted(alarmId);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }
}
