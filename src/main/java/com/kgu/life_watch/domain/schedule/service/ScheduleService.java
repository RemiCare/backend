package com.kgu.life_watch.domain.schedule.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.schedule.dto.request.ScheduleCreateRequest;
import com.kgu.life_watch.domain.schedule.entity.Schedule;
import com.kgu.life_watch.domain.schedule.repository.ScheduleRepository;
import com.kgu.life_watch.domain.user.entity.ElderlyProfile;
import com.kgu.life_watch.domain.user.repository.ElderlyProfileRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleService {
  private final ScheduleRepository scheduleRepository;
  private final ElderlyProfileRepository elderlyProfileRepository;

  // 어르신별 일정 목록 조회
  public List<Schedule> getScheduleList(Long elderlyProfileId) {
    return scheduleRepository.findAllByElderlyProfileId(elderlyProfileId);
  }

  // 일정 등록
  @Transactional
  public void createSchedule(ScheduleCreateRequest request) {
    ElderlyProfile elderly =
        elderlyProfileRepository
            .findById(request.elderlyId())
            .orElseThrow(() -> LifelineException.from(ErrorCode.SCHEDULE_NOT_FOUND));

    Schedule schedule =
        Schedule.createSchedule(
            request.title(),
            request.content(),
            request.executionTime(),
            request.daysOfWeek(),
            elderly);
    scheduleRepository.save(schedule);
  }

  // 일정 수정
  @Transactional
  public void updateSchedule(Long scheduleId, ScheduleCreateRequest request) {
    Schedule schedule =
        scheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.INFO_NOT_FOUND));

    schedule.updateSchedule(
        request.title(), request.content(), request.executionTime(), request.daysOfWeek());
  }

  // 일정 삭제
  @Transactional
  public void deleteSchedule(Long scheduleId) {
    scheduleRepository.deleteById(scheduleId);
  }
}
