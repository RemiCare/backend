package com.kgu.life_watch.domain.health.dto.response;

public class HealthDataResponse {

  private boolean success;
  private String message;

  public HealthDataResponse() {}

  public HealthDataResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public static HealthDataResponse success(String message) {
    return new HealthDataResponse(true, message);
  }

  public static HealthDataResponse fail(String message) {
    return new HealthDataResponse(false, message);
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
