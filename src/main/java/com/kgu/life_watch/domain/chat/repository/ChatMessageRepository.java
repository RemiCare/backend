package com.kgu.life_watch.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kgu.life_watch.domain.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :roomId ORDER BY m.createdAt ASC")
  List<ChatMessage> findAllByChatRoomId(@Param("roomId") Long roomId);

  Optional<ChatMessage> findTopByChatRoomIdOrderByCreatedAtDesc(Long roomId);
}
