package com.kgu.life_watch.domain.notification.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long elderlyId;
  private String title;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String message;

  private LocalDateTime sentAt;

  public static NotificationLog of(Long elderlyId, String title, String message) {
    return NotificationLog.builder()
        .elderlyId(elderlyId)
        .title(title)
        .message(message)
        .sentAt(LocalDateTime.now())
        .build();
  }
}
