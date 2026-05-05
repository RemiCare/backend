package com.kgu.life_watch.domain.medication.entity;

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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Medication extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name; // 약 이름

  @Column(nullable = false)
  private LocalTime dosageTime; // 복용 시간

  @ElementCollection
  @CollectionTable(name = "medication_days", joinColumns = @JoinColumn(name = "medication_id"))
  @Enumerated(EnumType.STRING)
  private List<DayOfWeek> daysOfWeek = new ArrayList<>(); // 복용 요일 (예: 월, 수, 금)

  private boolean isTaken; // 복용 여부 (기본 false)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "elderly_profile_id", nullable = false)
  private ElderlyProfile elderlyProfile;

  public static Medication createMedication(
      String name,
      LocalTime dosageTime,
      List<DayOfWeek> daysOfWeek,
      ElderlyProfile elderlyProfile) {
    Medication medication = new Medication();
    medication.name = name;
    medication.dosageTime = dosageTime;
    medication.daysOfWeek = new ArrayList<>(daysOfWeek);
    medication.elderlyProfile = elderlyProfile;
    medication.isTaken = false;
    return medication;
  }

  // 복용 상태 업데이트 (어르신 확인 혹은 보호자 수동 설정)
  public void updateTakenStatus(boolean isTaken) {
    this.isTaken = isTaken;
  }
}
