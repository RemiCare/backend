package com.kgu.life_watch.domain.alarm.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
    name = "alarm_push_token",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"expo_push_token"})})
public class AlarmPushToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "expo_push_token", nullable = false, length = 255)
  private String expoPushToken;

  @Column(name = "platform", length = 30)
  private String platform;

  @Column(name = "device_name", length = 100)
  private String deviceName;

  @Column(name = "enabled", nullable = false)
  private Boolean enabled = true;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public void updateTokenInfo(Long userId, String platform, String deviceName) {
    this.userId = userId;
    this.platform = platform;
    this.deviceName = deviceName;
    this.enabled = true;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public String getExpoPushToken() {
    return expoPushToken;
  }

  public String getPlatform() {
    return platform;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setExpoPushToken(String expoPushToken) {
    this.expoPushToken = expoPushToken;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
