package com.kgu.life_watch.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.notification.entity.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
  List<NotificationLog> findByElderlyIdOrderBySentAtDesc(Long elderlyId);
}
