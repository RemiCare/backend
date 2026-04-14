package com.kgu.life_watch.global.exception;

import lombok.Getter;

@Getter
public class LifelineException extends RuntimeException {
  private final ErrorCode errorCode;
  private String message;

  private LifelineException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  private LifelineException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
    this.message = message;
  }

  public static LifelineException from(ErrorCode errorCode) {
    return new LifelineException(errorCode);
  }

  public static LifelineException from(ErrorCode errorCode, String message) {
    return new LifelineException(errorCode, message);
  }
}
