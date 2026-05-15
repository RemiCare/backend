package com.kgu.life_watch.domain.alarm.dto.response;

public class AlarmResponse {

  private boolean success;
  private String message;

  public AlarmResponse() {}

  public AlarmResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public static AlarmResponse success(String message) {
    return new AlarmResponse(true, message);
  }

  public static AlarmResponse fail(String message) {
    return new AlarmResponse(false, message);
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
