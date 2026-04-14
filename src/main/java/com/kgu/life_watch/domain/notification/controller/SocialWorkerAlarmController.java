package com.kgu.life_watch.domain.notification.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.notification.dto.AlarmGroupDto;
import com.kgu.life_watch.domain.notification.dto.MedicineAlarmRequest;
import com.kgu.life_watch.domain.notification.repository.MedicineAlarmRepository;
import com.kgu.life_watch.domain.notification.service.AlarmGroupService;
import com.kgu.life_watch.domain.notification.validator.ElderlyAssignmentValidator;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;
import com.kgu.life_watch.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/social-worker/alarm")
@Tag(name = "SocialWorkerAlarmController", description = "복지사 전용 약 알람 관리 API")
public class SocialWorkerAlarmController {

  private final ElderlyAssignmentValidator validator;
  private final AlarmGroupService alarmGroupService;
  private final MedicineAlarmRepository medicineAlarmRepository;

  @PostMapping("/{elderlyId}/group")
  @Operation(summary = "복지사 - 알람 그룹 생성", description = "담당 노인을 위한 약 알람 그룹을 생성합니다.")
  public ApiResponse<Void> createGroup(
      @PathVariable Long elderlyId,
      @RequestBody @Valid MedicineAlarmRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    alarmGroupService.createAlarmGroup(
        request, validator.validate(userDetails.user(), elderlyId).getUser());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PutMapping("/{groupId}/alarm")
  @Operation(summary = "복지사 - 알람 그룹 수정", description = "복지사가 약 알람 그룹의 시간 및 주기를 수정합니다.")
  public ApiResponse<Void> reschedule(
      @PathVariable Long groupId,
      @RequestBody @Valid MedicineAlarmRequest request,
      @RequestParam Long elderlyId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    alarmGroupService.rescheduleGroup(
        groupId, request, validator.validate(userDetails.user(), elderlyId).getUser());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PatchMapping("/{groupId}")
  @Operation(summary = "복지사 - 그룹 이름/주의사항 수정", description = "복지사가 그룹 이름 또는 주의사항을 수정합니다.")
  public ApiResponse<Void> updateGroupInfo(
      @PathVariable Long groupId,
      @RequestParam @NotBlank String newName,
      @RequestParam String newNote,
      @RequestParam Long elderlyId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    validator.validate(userDetails.user(), elderlyId);
    alarmGroupService.updateGroup(groupId, newName, newNote);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PostMapping("/{groupId}/alarm")
  @Operation(summary = "복지사 - 그룹에 알람 추가", description = "복지사가 기존 그룹에 알람 시간을 추가합니다.")
  public ApiResponse<Void> addAlarmToGroup(
      @PathVariable Long groupId,
      @RequestBody @Valid MedicineAlarmRequest request,
      @RequestParam Long elderlyId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    alarmGroupService.addAlarmToGroup(
        groupId, request, validator.validate(userDetails.user(), elderlyId).getUser());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @GetMapping("/{elderlyId}/alarms")
  @Operation(summary = "복지사 - 알람 목록 조회", description = "복지사가 담당 노인의 알람 그룹과 하위 알람을 모두 조회합니다.")
  public ApiResponse<AlarmGroupDto> getGroups(
      @PathVariable Long elderlyId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    var elderlyUser = validator.validate(userDetails.user(), elderlyId).getUser();
    List<AlarmGroupDto> groups = alarmGroupService.getAlarmGroups(elderlyUser);
    return new ApiResponse<>(groups);
  }

  @PatchMapping("/alarm/{alarmId}/complete")
  @Operation(summary = "복지사 - 개별 알람 복용 완료 처리", description = "복지사가 특정 약 알람을 복용 완료로 처리합니다.")
  public ApiResponse<Void> markSingleAlarmComplete(
      @PathVariable Long alarmId,
      @RequestParam Long elderlyId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    validator.validate(userDetails.user(), elderlyId);
    alarmGroupService.markAlarmAsCompleted(alarmId);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PatchMapping("/group/{groupId}/complete")
  @Operation(summary = "복지사 - 알람 그룹 전체 복용 완료 처리", description = "복지사가 해당 그룹 내 모든 알람을 복용 완료 처리합니다.")
  public ApiResponse<Void> markAllAlarmsInGroupComplete(
      @PathVariable Long groupId,
      @RequestParam Long elderlyId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    validator.validate(userDetails.user(), elderlyId);
    alarmGroupService.markAllAlarmsInGroupComplete(groupId);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @DeleteMapping("/group/{groupId}")
  @Operation(summary = "복지사 - 그룹 삭제", description = "복지사가 특정 알람 그룹(약 종류)을 삭제합니다.")
  public ApiResponse<Void> deleteGroup(
      @PathVariable Long groupId,
      @RequestParam Long elderlyId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    validator.validate(userDetails.user(), elderlyId);
    alarmGroupService.deleteGroup(groupId);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }
}
