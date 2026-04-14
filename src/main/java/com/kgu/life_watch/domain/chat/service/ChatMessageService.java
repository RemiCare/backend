package com.kgu.life_watch.domain.chat.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.kgu.life_watch.domain.chat.dto.ChatMessageDto;
import com.kgu.life_watch.domain.chat.dto.response.ChatMessageResponse;
import com.kgu.life_watch.domain.chat.entity.ChatMessage;
import com.kgu.life_watch.domain.chat.entity.ChatRoom;
import com.kgu.life_watch.domain.chat.repository.ChatMessageRepository;
import com.kgu.life_watch.domain.chat.repository.ChatParticipationRepository;
import com.kgu.life_watch.domain.chat.repository.ChatRoomRepository;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.UserRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatParticipationRepository chatParticipationRepository;
  private final UserRepository userRepository;

  @Transactional
  public ChatMessage createChatMessage(ChatMessageDto request) {

    User sender =
        userRepository
            .findById(request.userId())
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    ChatRoom chatRoom =
        chatRoomRepository
            .findById(request.roomId())
            .orElseThrow(() -> LifelineException.from(ErrorCode.CHAT_ROOM_NOT_FOUND));

    ChatMessage chatMessage = request.toEntity(chatRoom, sender);
    chatMessageRepository.save(chatMessage);

    chatRoom.addMessage(chatMessage);

    return chatMessage;
  }

  @Transactional
  public List<ChatMessageResponse> getMessagesByRoom(Long roomId, User user) {

    ChatRoom chatRoom =
        chatRoomRepository
            .findById(roomId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.CHAT_ROOM_NOT_FOUND));
    List<User> users = chatParticipationRepository.findUsersByChatRoom(chatRoom);
    boolean isParticipant =
        users.stream().anyMatch(findUser -> user.getId().equals(findUser.getId()));

    if (!isParticipant) {
      throw LifelineException.from(ErrorCode.CHAT_ROOM_NOT_OWNER);
    }

    List<ChatMessage> chatMessagesRoom = chatMessageRepository.findAllByChatRoomId(roomId);
    return ChatMessageResponse.fromEntitieList(chatMessagesRoom);
  }

  @Transactional
  public Optional<ChatMessage> getLastMessage(Long roomId) {
    return chatMessageRepository.findTopByChatRoomIdOrderByCreatedAtDesc(roomId);
  }
}
