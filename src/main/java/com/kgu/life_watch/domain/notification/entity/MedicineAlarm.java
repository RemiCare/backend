package com.kgu.life_watch.domain.notification.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.global.domain.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MedicineAlarm extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "medicine_name", nullable = false)
  private String medicineName;

  @Column(name = "time")
  private LocalDateTime time;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private AlarmStatus status;

  @Column(name = "medicine_note")
  private String medicineNote;

  @Enumerated(EnumType.STRING)
  @Column(name = "repeat_cycle", nullable = false)
  private RepeatCycle repeatCycle;

  @Column(nullable = false)
  private double dosage; // 복용량 (예: 2 알)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "alarm_group_id")
  private AlarmGroup alarmGroup;

  public void updateStatus(AlarmStatus newStatus) {
    this.status = newStatus;
  }

  public enum AlarmStatus {
    SCHEDULED,
    COMPLETE,
    MISSED
  }

  public enum RepeatCycle {
    ONCE, // 한 번만
    DAILY, // 매일
    EVERY_OTHER_DAY, // 이틀에 한 번
    WEEKLY // 일주일 간격
  }
}
