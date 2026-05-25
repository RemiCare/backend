package com.kgu.life_watch.domain.alarm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.alarm.entity.AlarmPushToken;

public interface AlarmPushTokenRepository extends JpaRepository<AlarmPushToken, Long> {

  Optional<AlarmPushToken> findByExpoPushToken(String expoPushToken);

  List<AlarmPushToken> findByUserIdAndEnabledTrue(Long userId);
}
