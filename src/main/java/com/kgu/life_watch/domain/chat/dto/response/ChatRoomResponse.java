package com.kgu.life_watch.domain.chat.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import com.kgu.life_watch.domain.chat.entity.ChatMessage;
import com.kgu.life_watch.domain.chat.entity.ChatRoom;
import com.kgu.life_watch.domain.user.entity.User;

@Builder
public record ChatRoomResponse(
    Long roomId,
    Long receiverId,
    String receiverName,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
    String lastMessage,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime lastMessageAt) {
  public static ChatRoomResponse fromEntity(
      ChatRoom savedChatRoom, User receiver, ChatMessage chatMessage) {
    return ChatRoomResponse.builder()
        .roomId(savedChatRoom.getId())
        .receiverId(receiver.getId())
        .receiverName(receiver.getName())
        .createdAt(savedChatRoom.getCreatedAt())
        .lastMessage(chatMessage != null ? chatMessage.getMessage() : null)
        .lastMessageAt(chatMessage != null ? chatMessage.getCreatedAt() : null)
        .build();
  }
}
