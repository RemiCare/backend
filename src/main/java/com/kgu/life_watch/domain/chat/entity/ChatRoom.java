package com.kgu.life_watch.domain.chat.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.kgu.life_watch.domain.chat.entity.mapping.ChatParticipation;

@Getter
@Entity
@Table
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  @Enumerated(EnumType.STRING)
  private RoomStatus status; // 채팅방 상태

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Builder.Default
  @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ChatParticipation> participation = new ArrayList<>(); // 채팅방의 참여 정보

  @Builder.Default
  @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ChatMessage> messages = new ArrayList<>();

  public List<ChatMessage> getChatMessages() {
    return messages;
  }

  public void addParticipation(ChatParticipation chatParticipation) {
    participation.add(chatParticipation);
  }

  public void addMessage(ChatMessage chatMessage) {
    messages.add(chatMessage);
  }

  public void updateStatus(RoomStatus roomStatus) {
    this.status = roomStatus;
  }
}
