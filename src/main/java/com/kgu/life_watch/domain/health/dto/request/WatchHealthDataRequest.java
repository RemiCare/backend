package com.kgu.life_watch.domain.health.dto.request;

import java.util.List;

public class WatchHealthDataRequest {

  private Long userId;

  private String currentDate;
  private Long currentSteps;
  private Integer currentHeartRate;
  private String currentHeartRateTime;
  private String lastUpdatedAt;

  private List<DailyHealthRow> dailyRows;

  public static class DailyHealthRow {
    private String date;
    private Long stepsTotal;
    private Integer heartRateMin;
    private Integer heartRateMax;
    private Integer heartRateAvg;
    private Long sleepMinutes;
    private Double sleepHours;
    private Long wakeMinutes;
    private Long remMinutes;
    private Long lightMinutes;
    private Long deepMinutes;

    public String getDate() {
      return date;
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

    public Long getSleepMinutes() {
      return sleepMinutes;
    }

    public Double getSleepHours() {
      return sleepHours;
    }

    public Long getWakeMinutes() {
      return wakeMinutes;
    }

    public Long getRemMinutes() {
      return remMinutes;
    }

    public Long getLightMinutes() {
      return lightMinutes;
    }

    public Long getDeepMinutes() {
      return deepMinutes;
    }

    public void setDate(String date) {
      this.date = date;
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

    public void setSleepMinutes(Long sleepMinutes) {
      this.sleepMinutes = sleepMinutes;
    }

    public void setSleepHours(Double sleepHours) {
      this.sleepHours = sleepHours;
    }

    public void setWakeMinutes(Long wakeMinutes) {
      this.wakeMinutes = wakeMinutes;
    }

    public void setRemMinutes(Long remMinutes) {
      this.remMinutes = remMinutes;
    }

    public void setLightMinutes(Long lightMinutes) {
      this.lightMinutes = lightMinutes;
    }

    public void setDeepMinutes(Long deepMinutes) {
      this.deepMinutes = deepMinutes;
    }
  }

  public Long getUserId() {
    return userId;
  }

  public String getCurrentDate() {
    return currentDate;
  }

  public Long getCurrentSteps() {
    return currentSteps;
  }

  public Integer getCurrentHeartRate() {
    return currentHeartRate;
  }

  public String getCurrentHeartRateTime() {
    return currentHeartRateTime;
  }

  public String getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  public List<DailyHealthRow> getDailyRows() {
    return dailyRows;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setCurrentDate(String currentDate) {
    this.currentDate = currentDate;
  }

  public void setCurrentSteps(Long currentSteps) {
    this.currentSteps = currentSteps;
  }

  public void setCurrentHeartRate(Integer currentHeartRate) {
    this.currentHeartRate = currentHeartRate;
  }

  public void setCurrentHeartRateTime(String currentHeartRateTime) {
    this.currentHeartRateTime = currentHeartRateTime;
  }

  public void setLastUpdatedAt(String lastUpdatedAt) {
    this.lastUpdatedAt = lastUpdatedAt;
  }

  public void setDailyRows(List<DailyHealthRow> dailyRows) {
    this.dailyRows = dailyRows;
  }
}
