package com.kgu.life_watch.domain.chat.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.kgu.life_watch.domain.chat.dto.ChatMessageDto;
import com.kgu.life_watch.domain.chat.dto.response.ChatMessageResponse;
import com.kgu.life_watch.domain.chat.entity.ChatMessage;
import com.kgu.life_watch.domain.chat.service.ChatMessageService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

  private final ChatMessageService chatMessageService;
  private final RabbitTemplate rabbitTemplate;

  @Value("${rabbitmq.chat.exchange.name}")
  private String CHAT_EXCHANGE_NAME;

  @MessageMapping("chat.message")
  public void sendMessage(@Valid ChatMessageDto request) {
    // 실시간으로 방에서 채팅하기
    ChatMessage newChatMessage = chatMessageService.createChatMessage(request);
    log.info("received message: {}", request);

    // 방에 있는 모든 사용자에게 메시지 전송
    rabbitTemplate.convertAndSend(
        CHAT_EXCHANGE_NAME,
        "room." + request.roomId(),
        ChatMessageResponse.fromEntity(newChatMessage));
  }
}
