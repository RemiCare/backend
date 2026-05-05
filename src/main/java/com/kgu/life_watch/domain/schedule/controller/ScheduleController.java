package com.kgu.life_watch.domain.schedule.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.schedule.dto.request.ScheduleCreateRequest;
import com.kgu.life_watch.domain.schedule.dto.response.ScheduleResponse;
import com.kgu.life_watch.domain.schedule.service.ScheduleService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
@Tag(name = "ScheduleController", description = "일정 관리 관련 API")
public class ScheduleController {

  private final ScheduleService scheduleService;

  @PostMapping
  @Operation(summary = "일정 등록", description = "어르신의 새로운 일정을 등록합니다.")
  public ApiResponse<Void> createSchedule(@RequestBody @Valid ScheduleCreateRequest request) {
    scheduleService.createSchedule(request);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @GetMapping("/elderly/{elderlyId}")
  @Operation(summary = "어르신별 일정 목록 조회", description = "특정 어르신의 모든 일정을 조회합니다.")
  public ApiResponse<ScheduleResponse> getSchedules(@PathVariable Long elderlyId) {
    List<ScheduleResponse> response =
        scheduleService.getScheduleList(elderlyId).stream().map(ScheduleResponse::from).toList();
    return new ApiResponse<ScheduleResponse>(response);
  }

  @PatchMapping("/{scheduleId}")
  @Operation(summary = "일정 수정", description = "등록된 일정을 수정합니다.")
  public ApiResponse<Void> updateSchedule(
      @PathVariable Long scheduleId, @RequestBody @Valid ScheduleCreateRequest request) {
    scheduleService.updateSchedule(scheduleId, request);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @DeleteMapping("/{scheduleId}")
  @Operation(summary = "일정 삭제", description = "등록된 일정을 삭제합니다.")
  public ApiResponse<Void> deleteSchedule(@PathVariable Long scheduleId) {
    scheduleService.deleteSchedule(scheduleId);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }
}
