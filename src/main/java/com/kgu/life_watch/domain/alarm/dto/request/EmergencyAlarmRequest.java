package com.kgu.life_watch.domain.alarm.dto.request;

public class EmergencyAlarmRequest {

  private Long userId;
  private String level;
  private String title;
  private String body;
  private String type;

  public Long getUserId() {
    return userId;
  }

  public String getLevel() {
    return level;
  }

  public String getTitle() {
    return title;
  }

  public String getBody() {
    return body;
  }

  public String getType() {
    return type;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public void setType(String type) {
    this.type = type;
  }
}
