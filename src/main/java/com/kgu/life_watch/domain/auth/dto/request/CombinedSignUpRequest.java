package com.kgu.life_watch.domain.auth.dto.request;

import jakarta.validation.Valid;

public record CombinedSignUpRequest(
    @Valid ProtectorSignUpRequest protector, @Valid ElderlySignUpRequest elderly) {}
