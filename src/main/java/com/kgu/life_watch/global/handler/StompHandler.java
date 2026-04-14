package com.kgu.life_watch.global.handler;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.UserRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;
import com.kgu.life_watch.global.jwt.JwtTokenProvider;

@RequiredArgsConstructor
@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  //  @Override
  //  public Message<?> preSend(Message<?> message, MessageChannel channel) {
  //    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
  //
  //    if (accessor.getCommand() == StompCommand.CONNECT) {
  //      String accessToken = accessor.getFirstNativeHeader("Authorization");
  //
  //      if (accessToken == null) {
  //        throw LifelineException.from(ErrorCode.INVALID_AUTH_TOKEN);
  //      }
  //
  //      accessToken = resolveToken(accessToken);
  //
  //      if (!jwtTokenProvider.validateToken(accessToken)) {
  //        throw LifelineException.from(ErrorCode.INVALID_AUTH_TOKEN);
  //      }
  //
  //      if (accessToken == null) {
  //        throw LifelineException.from(ErrorCode.INVALID_AUTH_TOKEN);
  //      }
  //
  //      String loginId = jwtTokenProvider.getLoginIdFromToken(accessToken);
  //      User findUser = userRepository.findByLoginId(loginId).orElse(null);
  //
  //      if (findUser == null) {
  //        throw LifelineException.from(ErrorCode.MEMBER_NOT_FOUND);
  //      }
  //      accessor.getSessionAttributes().put("senderUserId", findUser.getId().toString());
  //    }
  //
  //    return message;
  //  }
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (accessor.getCommand() == StompCommand.CONNECT) {
      String accessToken = accessor.getFirstNativeHeader("Authorization");

      if (accessToken == null) {
        throw LifelineException.from(ErrorCode.INVALID_AUTH_TOKEN);
      }

      accessToken = resolveToken(accessToken);

      if (!jwtTokenProvider.validateToken(accessToken)) {
        throw LifelineException.from(ErrorCode.INVALID_AUTH_TOKEN);
      }

      if (accessToken == null) {
        throw LifelineException.from(ErrorCode.INVALID_AUTH_TOKEN);
      }

      String loginId = jwtTokenProvider.getLoginIdFromToken(accessToken);

      User findUser = userRepository.findByLoginId(loginId).orElse(null);

      if (findUser == null) {
        throw LifelineException.from(ErrorCode.MEMBER_NOT_FOUND);
      }

      accessor.getSessionAttributes().put("senderUserId", findUser.getId().toString());
    }

    return message;
  }

  private String resolveToken(String bearerToken) {
    // Bearer 토큰 파싱
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
