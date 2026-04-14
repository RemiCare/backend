package com.kgu.life_watch.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.global.handler.StompHandler;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final StompHandler stompHandler;

  // RabbitMQ 연결에 필요한 값들을 @Value로 설정
  @Value("${rabbitmq.host}")
  private String host;

  @Value("${rabbitmq.relay.port}")
  private int port;

  @Value("${rabbitmq.relay.system-login}")
  private String systemLogin;

  @Value("${rabbitmq.relay.client-passcode}")
  private String systemPasscode;

  @Value("${rabbitmq.relay.client-login}")
  private String clientLogin;

  @Value("${rabbitmq.relay.client-passcode}")
  private String clientPasscode;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {

    registry
        .addEndpoint("/chat")
        .setAllowedOriginPatterns(
            "https://server.lifewatch.store",
            "https://jiangxy.github.io",
            "http://localhost:3000",
            "http://localhost:8080",
            "https://dqm6a810qt4d9.cloudfront.net",
            "https://www.lifewatch.store")
        .withSockJS(); // 추후 배포시 수정 필요

    // 안드로이드용 (WebSocket 직접 연결)
    registry
        .addEndpoint("/ws-chat")
        .setAllowedOriginPatterns("*"); // 개발 시엔 *, 배포 시 안드로이드 Origin에 맞게 설정
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {

    // RabbitMQ와 STOMP Broker Relay 설정
    registry.setPathMatcher(new AntPathMatcher("."));

    // RabbitMQ와 STOMP Broker Relay 설정
    registry
        .enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
        .setRelayHost(host) // RabbitMQ 호스트
        .setRelayPort(port) // RabbitMQ 포트
        .setSystemLogin(systemLogin) // 시스템 로그인
        .setSystemPasscode(systemPasscode) // 시스템 비밀번호
        .setClientLogin(clientLogin) // 클라이언트 로그인
        .setClientPasscode(clientPasscode); // 클라이언트 비밀번호

    // 클라이언트로부터 메시지를 받을 api의 prefix를 설정한다.
    // publish
    registry.setApplicationDestinationPrefixes("/pub");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(stompHandler);
  }
}
