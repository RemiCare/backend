package com.kgu.life_watch.domain.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.chat.entity.ChatRoom;
import com.kgu.life_watch.domain.chat.entity.mapping.ChatParticipation;
import com.kgu.life_watch.domain.chat.repository.ChatParticipationRepository;
import com.kgu.life_watch.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class ChatParticipationService {

  private final ChatParticipationRepository chatParticipationRepository;

  public void joinRoom(ChatRoom chatRoom, User me, User you) {
    ChatParticipation participation1 =
        chatParticipationRepository.save(
            ChatParticipation.builder().chatRoom(chatRoom).user(me).build());
    ChatParticipation participation2 =
        chatParticipationRepository.save(
            ChatParticipation.builder().chatRoom(chatRoom).user(you).build());
    chatRoom.addParticipation(participation1);
    chatRoom.addParticipation(participation2);
  }

  public List<ChatParticipation> getParticipationByUser(User user) {
    return chatParticipationRepository.findAllByUserId(user.getId());
  }
}
