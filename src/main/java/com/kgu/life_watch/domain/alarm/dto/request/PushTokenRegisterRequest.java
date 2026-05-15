package com.kgu.life_watch.domain.alarm.dto.request;

public class PushTokenRegisterRequest {

  private Long userId;
  private String expoPushToken;
  private String platform;
  private String deviceName;

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
}