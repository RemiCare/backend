package com.kgu.life_watch.domain.auth.dto.response;

import lombok.Builder;

@Builder
public record SmsVerifyResponse(Boolean isValid) {}
