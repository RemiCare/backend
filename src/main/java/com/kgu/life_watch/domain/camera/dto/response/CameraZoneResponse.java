package com.kgu.life_watch.domain.camera.dto.response;

import com.kgu.life_watch.domain.camera.entity.CameraConfig;

public record CameraZoneResponse(
    Long elderlyId,
    Double bX,
    Double bY,
    Double bW,
    Double bH,
    Double cX,
    Double cY,
    Double cW,
    Double cH) {
  public static CameraZoneResponse from(CameraConfig config) {
    return new CameraZoneResponse(
        config.getElderlyId(),
        config.getBX(),
        config.getBY(),
        config.getBW(),
        config.getBH(),
        config.getCX(),
        config.getCY(),
        config.getCW(),
        config.getCH());
  }
}
