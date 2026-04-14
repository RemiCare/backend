package com.kgu.life_watch.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import com.kgu.life_watch.domain.chat.entity.ChatMessage;
import com.kgu.life_watch.domain.chat.entity.ChatRoom;
import com.kgu.life_watch.domain.user.entity.User;

@Builder
public record ChatMessageDto(
    Long userId,
    Long roomId,
    @NotBlank(message = "메시지는 필수입니다.")
        @Size(min = 1, max = 200, message = "메시지는 최소 1자, 최대 200자까지 입력 가능합니다.")
        String message) {
  /* Dto -> Entity */
  public ChatMessage toEntity(ChatRoom chatRoom, User sender) {
    return ChatMessage.builder()
        .chatRoom(chatRoom)
        .sender(sender)
        .senderName(sender.getName())
        .message(message)
        .build();
  }
}
