package com.kgu.life_watch.domain.health.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
    name = "health_data",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "record_date"})})
public class HealthData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "record_date", nullable = false)
  private LocalDate recordDate;

  @Column(name = "steps_total")
  private Long stepsTotal;

  @Column(name = "heart_rate_min")
  private Integer heartRateMin;

  @Column(name = "heart_rate_max")
  private Integer heartRateMax;

  @Column(name = "heart_rate_avg")
  private Integer heartRateAvg;

  @Column(name = "current_heart_rate")
  private Integer currentHeartRate;

  @Column(name = "current_heart_rate_time")
  private LocalDateTime currentHeartRateTime;

  @Column(name = "sleep_minutes")
  private Long sleepMinutes;

  @Column(name = "sleep_hours")
  private Double sleepHours;

  @Column(name = "wake_minutes")
  private Long wakeMinutes = 0L;

  @Column(name = "rem_minutes")
  private Long remMinutes = 0L;

  @Column(name = "light_minutes")
  private Long lightMinutes = 0L;

  @Column(name = "deep_minutes")
  private Long deepMinutes = 0L;

  @Column(name = "active_calories")
  private Double activeCalories = 0.0;

  @Column(name = "distance")
  private Double distance = 0.0;

  @Column(name = "respiratory_rate")
  private Integer respiratoryRate;

  @Column(name = "last_updated_at")
  private LocalDateTime lastUpdatedAt;

  public void updateDailyData(
      Long stepsTotal,
      Integer heartRateMin,
      Integer heartRateMax,
      Integer heartRateAvg,
      Long sleepMinutes,
      Double sleepHours,
      LocalDateTime lastUpdatedAt) {
    this.stepsTotal = stepsTotal;
    this.heartRateMin = heartRateMin;
    this.heartRateMax = heartRateMax;
    this.heartRateAvg = heartRateAvg;
    this.sleepMinutes = sleepMinutes;
    this.sleepHours = sleepHours;
    this.lastUpdatedAt = lastUpdatedAt;
  }

  public void updateCurrentHeartRate(Integer currentHeartRate, LocalDateTime currentHeartRateTime) {
    this.currentHeartRate = currentHeartRate;
    this.currentHeartRateTime = currentHeartRateTime;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public LocalDate getRecordDate() {
    return recordDate;
  }

  public Long getStepsTotal() {
    return stepsTotal;
  }

  public Integer getHeartRateMin() {
    return heartRateMin;
  }

  public Integer getHeartRateMax() {
    return heartRateMax;
  }

  public Integer getHeartRateAvg() {
    return heartRateAvg;
  }

  public Integer getCurrentHeartRate() {
    return currentHeartRate;
  }

  public LocalDateTime getCurrentHeartRateTime() {
    return currentHeartRateTime;
  }

  public Long getSleepMinutes() {
    return sleepMinutes;
  }

  public Double getSleepHours() {
    return sleepHours;
  }

  public LocalDateTime getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setRecordDate(LocalDate recordDate) {
    this.recordDate = recordDate;
  }

  public void setStepsTotal(Long stepsTotal) {
    this.stepsTotal = stepsTotal;
  }

  public void setHeartRateMin(Integer heartRateMin) {
    this.heartRateMin = heartRateMin;
  }

  public void setHeartRateMax(Integer heartRateMax) {
    this.heartRateMax = heartRateMax;
  }

  public void setHeartRateAvg(Integer heartRateAvg) {
    this.heartRateAvg = heartRateAvg;
  }

  public void setCurrentHeartRate(Integer currentHeartRate) {
    this.currentHeartRate = currentHeartRate;
  }

  public void setCurrentHeartRateTime(LocalDateTime currentHeartRateTime) {
    this.currentHeartRateTime = currentHeartRateTime;
  }

  public void setSleepMinutes(Long sleepMinutes) {
    this.sleepMinutes = sleepMinutes;
  }

  public void setSleepHours(Double sleepHours) {
    this.sleepHours = sleepHours;
  }

  public Long getWakeMinutes() {
    return wakeMinutes;
  }

  public void setWakeMinutes(Long wakeMinutes) {
    this.wakeMinutes = wakeMinutes;
  }

  public Long getRemMinutes() {
    return remMinutes;
  }

  public void setRemMinutes(Long remMinutes) {
    this.remMinutes = remMinutes;
  }

  public Long getLightMinutes() {
    return lightMinutes;
  }

  public void setLightMinutes(Long lightMinutes) {
    this.lightMinutes = lightMinutes;
  }

  public Long getDeepMinutes() {
    return deepMinutes;
  }

  public void setDeepMinutes(Long deepMinutes) {
    this.deepMinutes = deepMinutes;
  }

  public Double getActiveCalories() {
    return activeCalories;
  }

  public void setActiveCalories(Double activeCalories) {
    this.activeCalories = activeCalories;
  }

  public Double getDistance() {
    return distance;
  }

  public void setDistance(Double distance) {
    this.distance = distance;
  }

  public Integer getRespiratoryRate() {
    return respiratoryRate;
  }

  public void setRespiratoryRate(Integer respiratoryRate) {
    this.respiratoryRate = respiratoryRate;
  }

  public void addSleepStageMinutes(SleepStage stage, long minutes) {
    if (stage == null) return;
    if (this.wakeMinutes == null) this.wakeMinutes = 0L;
    if (this.remMinutes == null) this.remMinutes = 0L;
    if (this.lightMinutes == null) this.lightMinutes = 0L;
    if (this.deepMinutes == null) this.deepMinutes = 0L;

    switch (stage) {
      case WAKE:
        this.wakeMinutes += minutes;
        break;
      case REM:
        this.remMinutes += minutes;
        break;
      case LIGHT:
        this.lightMinutes += minutes;
        break;
      case DEEP:
        this.deepMinutes += minutes;
        break;
    }
  }

  public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
    this.lastUpdatedAt = lastUpdatedAt;
  }
}
