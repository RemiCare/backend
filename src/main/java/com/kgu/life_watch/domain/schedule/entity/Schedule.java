package com.kgu.life_watch.domain.schedule.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.kgu.life_watch.domain.user.entity.ElderlyProfile;
import com.kgu.life_watch.global.domain.BaseEntity;

@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title; // 일정 제목

  private String content; // 일정 상세 내용

  @Column(nullable = false)
  private LocalTime executionTime; // 실행 시간

  @ElementCollection
  @CollectionTable(name = "schedule_days", joinColumns = @JoinColumn(name = "schedule_id"))
  @Enumerated(EnumType.STRING)
  private List<DayOfWeek> daysOfWeek = new ArrayList<>(); // 반복 요일

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "elderly_profile_id", nullable = false)
  private ElderlyProfile elderlyProfile;

  // 생성 메서드
  public static Schedule createSchedule(
      String title,
      String content,
      LocalTime executionTime,
      List<DayOfWeek> daysOfWeek,
      ElderlyProfile elderlyProfile) {
    Schedule schedule = new Schedule();
    schedule.title = title;
    schedule.content = content;
    schedule.executionTime = executionTime;
    schedule.daysOfWeek = new ArrayList<>(daysOfWeek);
    schedule.elderlyProfile = elderlyProfile;
    return schedule;
  }

  // 일정 수정 메서드
  public void updateSchedule(
      String title, String content, LocalTime executionTime, List<DayOfWeek> daysOfWeek) {
    this.title = title;
    this.content = content;
    this.executionTime = executionTime;
    this.daysOfWeek = new ArrayList<>(daysOfWeek);
  }
}
