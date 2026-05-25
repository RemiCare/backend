package com.kgu.life_watch.domain.schedule.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import com.kgu.life_watch.domain.schedule.entity.Schedule;

public record ScheduleResponse(
    Long id, String title, String content, LocalTime executionTime, List<DayOfWeek> daysOfWeek) {
  public static ScheduleResponse from(Schedule schedule) {
    return new ScheduleResponse(
        schedule.getId(),
        schedule.getTitle(),
        schedule.getContent(),
        schedule.getExecutionTime(),
        schedule.getDaysOfWeek());
  }
}
