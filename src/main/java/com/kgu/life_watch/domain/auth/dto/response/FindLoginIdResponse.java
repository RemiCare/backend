package com.kgu.life_watch.domain.auth.dto.response;

import lombok.Builder;

@Builder
public record FindLoginIdResponse(String loginId) {}
