package com.kgu.life_watch.domain.camera.dto.request;

import jakarta.validation.constraints.NotNull;

public record CameraZoneRequest(
    @NotNull(message = "어르신 ID는 필수입니다.") Long elderlyId,

    // B구역 (거실)
    @NotNull Double bX,
    @NotNull Double bY,
    @NotNull Double bW,
    @NotNull Double bH,

    // C구역 (화장실)
    @NotNull Double cX,
    @NotNull Double cY,
    @NotNull Double cW,
    @NotNull Double cH) {}
