package com.kgu.life_watch.domain.auth.dto.response;

public record CombinedSignUpResponse(
    String protectorName, String elderlyName, String elderlyLoginCode // 보호자가 어르신 폰에 입력해줄 코드
    ) {}
