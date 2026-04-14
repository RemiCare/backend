package com.kgu.life_watch.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {}
