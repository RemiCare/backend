package com.kgu.life_watch.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmergencyAlertRequest(
    @NotNull Long elderlyId, @NotBlank String predictionLabel, @NotBlank String explanation) {}
