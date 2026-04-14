package com.kgu.life_watch.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kgu.life_watch.domain.chat.entity.ChatRoom;
import com.kgu.life_watch.domain.chat.entity.mapping.ChatParticipation;
import com.kgu.life_watch.domain.user.entity.User;

public interface ChatParticipationRepository extends JpaRepository<ChatParticipation, Long> {

  @Query("SELECT cp FROM ChatParticipation cp WHERE cp.user.id = :userId")
  List<ChatParticipation> findAllByUserId(Long userId);

  @Query("SELECT cp.user FROM ChatParticipation cp WHERE cp.chatRoom = :chatRoom")
  List<User> findUsersByChatRoom(ChatRoom chatRoom);
}
