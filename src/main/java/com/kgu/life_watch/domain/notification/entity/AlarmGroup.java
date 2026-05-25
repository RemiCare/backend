package com.kgu.life_watch.domain.notification.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.global.domain.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlarmGroup extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String medicineName;

  private String medicineNote;

  @Enumerated(EnumType.STRING)
  private MedicineAlarm.RepeatCycle repeatCycle;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @OneToMany(mappedBy = "alarmGroup", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<MedicineAlarm> alarms = new ArrayList<>();

  // 내용 업데이트
  public void updateInfo(String name, String note) {
    this.medicineName = name;
    this.medicineNote = note;
  }

  // 복용 주기 업데이트
  public void updateRepeatCycle(MedicineAlarm.RepeatCycle cycle) {
    this.repeatCycle = cycle;
  }
}
