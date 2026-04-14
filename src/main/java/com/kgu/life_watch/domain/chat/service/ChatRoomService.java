package com.kgu.life_watch.domain.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.kgu.life_watch.domain.chat.dto.response.ChatRoomResponse;
import com.kgu.life_watch.domain.chat.entity.ChatMessage;
import com.kgu.life_watch.domain.chat.entity.ChatRoom;
import com.kgu.life_watch.domain.chat.entity.RoomStatus;
import com.kgu.life_watch.domain.chat.entity.mapping.ChatParticipation;
import com.kgu.life_watch.domain.chat.repository.ChatRoomRepository;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.UserRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

  private final UserRepository userRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatParticipationService chatParticipationService;
  private final ChatMessageService chatMessageService;

  /*
  채팅방 생성 + 참여자 추가
  */
  @Transactional
  public ChatRoomResponse createChatRoom(User user, Long receiverId) {

    if (user == null) {
      throw LifelineException.from(ErrorCode.INVALID_REQUEST);
    }
    if (Objects.equals(receiverId, user.getId())) {
      throw LifelineException.from(ErrorCode.CHAT_ROOM_SELF);
    }

    User receiver =
        userRepository
            .findById(receiverId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    // 이미 해당 룸메이트와 채팅방을 만든 적이 있다면 채팅방 정보를 바로 리턴
    List<ChatParticipation> chatParticipationList =
        chatParticipationService.getParticipationByUser(user);

    for (ChatParticipation chatParticipation : chatParticipationList) {
      ChatRoom chatRoom = chatParticipation.getChatRoom();

      List<Long> participantIds =
          chatRoom.getParticipation().stream().map(p -> p.getUser().getId()).toList();

      if (participantIds.size() == 2
          && participantIds.contains(user.getId())
          && participantIds.contains(receiver.getId())) {

        // 여기서 상태가 DEACTIVATE이면 ACTIVATE로 변경
        if (chatRoom.getStatus() == RoomStatus.DEACTIVATE) {
          chatRoom.updateStatus(RoomStatus.ACTIVATE);
        }

        return ChatRoomResponse.fromEntity(
            chatRoom, receiver, chatMessageService.getLastMessage(chatRoom.getId()).orElse(null));
      }
    }
    ChatRoom chatRoom =
        ChatRoom.builder().status(RoomStatus.ACTIVATE).createdAt(LocalDateTime.now()).build();

    // 채팅방 생성 후 저장
    ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

    // 생성된 채팅방의 참여자로 나랑 룸메를 저장
    chatParticipationService.joinRoom(chatRoom, user, receiver);

    return ChatRoomResponse.fromEntity(savedChatRoom, receiver, null);
  }

  @Transactional
  public List<ChatRoomResponse> getAllRoom(User user) {

    List<ChatParticipation> chatParticipationList =
        chatParticipationService.getParticipationByUser(user);
    List<ChatRoomResponse> chatRooms = new ArrayList<>();

    for (ChatParticipation chatParticipation : chatParticipationList) {

      Long roomId = chatParticipation.getChatRoom().getId();
      ChatRoom chatRoom =
          chatRoomRepository
              .findById(roomId)
              .orElseThrow(() -> LifelineException.from(ErrorCode.CHAT_ROOM_NOT_FOUND));

      Optional<ChatMessage> chatMessage = chatMessageService.getLastMessage(roomId);
      List<ChatParticipation> usersInRoom = chatRoom.getParticipation();
      // 상대 유저 객체를 추출
      User receiver =
          Objects.equals(usersInRoom.get(0).getUser().getId(), user.getId())
              ? usersInRoom.get(1).getUser()
              : usersInRoom.get(0).getUser();

      chatRooms.add(ChatRoomResponse.fromEntity(chatRoom, receiver, chatMessage.orElse(null)));
    }
    chatRooms.sort(
        (o1, o2) -> {
          // o2의 메시지가 null인지 확인하고, null일 경우 생성시간으로 비교
          LocalDateTime time1 = o1.lastMessageAt() != null ? o1.lastMessageAt() : o1.createdAt();
          LocalDateTime time2 = o2.lastMessageAt() != null ? o2.lastMessageAt() : o2.createdAt();

          return time2.compareTo(time1); // 최근 시간 순으로 정렬
        });
    return chatRooms;
  }

  @Transactional
  // 유저들끼리 대화 중 문제가 생길 경우, 확인해야할 수 있으니 soft delete 만 지행
  public void deleteRoom(Long roomId, User user) {

    ChatRoom chatRoom =
        chatRoomRepository
            .findById(roomId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.CHAT_ROOM_NOT_FOUND));
    boolean flag = false;
    for (ChatParticipation chatParticipation : chatRoom.getParticipation()) {
      if (Objects.equals(chatParticipation.getUser().getId(), user.getId())) {
        flag = true;
        break;
      }
    }
    if (!flag) {
      throw LifelineException.from(ErrorCode.CHAT_ROOM_NOT_PARTICIPANT);
    }

    chatRoom.updateStatus(RoomStatus.DEACTIVATE); // `deactivate`로 변경하면서 `soft delete`만 진행
  }

  @Transactional
  public ChatRoom getChatRoom(Long roomId) {
    return chatRoomRepository
        .findById(roomId)
        .orElseThrow(() -> LifelineException.from(ErrorCode.CHAT_ROOM_NOT_FOUND));
  }
}
