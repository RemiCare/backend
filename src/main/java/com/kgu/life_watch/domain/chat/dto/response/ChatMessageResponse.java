package com.kgu.life_watch.domain.chat.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;

import com.kgu.life_watch.domain.chat.entity.ChatMessage;

public record ChatMessageResponse(
    @Size(min = 1, max = 200, message = "메시지는 최소 1자, 최대 200자까지 입력 가능합니다.") String message,
    Long senderId,
    Long roomId,
    String senderName,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt) {

  public static ChatMessageResponse fromEntity(ChatMessage chatMessage) {
    return new ChatMessageResponse(
        chatMessage.getMessage(),
        chatMessage.getSender().getId(),
        chatMessage.getChatRoom().getId(),
        chatMessage.getSenderName(),
        chatMessage.getCreatedAt());
  }

  public static List<ChatMessageResponse> fromEntitieList(List<ChatMessage> chatMessages) {
    return chatMessages.stream().map(ChatMessageResponse::fromEntity).toList();
  }
}
