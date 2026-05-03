package com.kgu.life_watch.domain.health.entity;

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
@Table(name = "health_data")
public class HealthData extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 생체 데이터 주인
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 측정 시각
  @Column(name = "measured_at", nullable = false)
  private LocalDateTime measuredAt;

  // 기기명
  @Column(name = "device_name")
  private String deviceName;

  // 심박수 bpm
  @Column(name = "heart_rate")
  private Integer heartRate;

  // 걸음 수
  @Column(name = "step_count")
  private Integer stepCount;

  // 혈중산소 %
  @Column(name = "blood_oxygen")
  private Integer bloodOxygen;

  // 총 수면 시간, 분 단위
  @Column(name = "total_sleep_minutes")
  private Integer totalSleepMinutes;

  // 깨어있던 시간, 분 단위
  @Column(name = "awake_minutes")
  private Integer awakeMinutes;

  // 원본 JSON 저장용
  @Lob
  @Column(name = "raw_data", columnDefinition = "TEXT")
  private String rawData;
}