package com.kgu.life_watch.domain.schedule.repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kgu.life_watch.domain.schedule.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  // 어르신별 일정 목록 조회
  List<Schedule> findAllByElderlyProfileId(Long elderlyProfileId);

  // 알림용: 특정 시간과 요일에 해당하는 일정 조회
  @Query("SELECT s FROM Schedule s JOIN s.daysOfWeek d WHERE s.executionTime = :time AND d = :day")
  List<Schedule> findAllByExecutionTimeAndDayOfWeek(
      @Param("time") LocalTime time, @Param("day") DayOfWeek day);
}
