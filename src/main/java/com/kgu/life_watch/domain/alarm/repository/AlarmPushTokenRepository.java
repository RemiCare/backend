package com.kgu.life_watch.domain.alarm.repository;

import com.kgu.life_watch.domain.alarm.entity.AlarmPushToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmPushTokenRepository extends JpaRepository<AlarmPushToken, Long> {

  Optional<AlarmPushToken> findByExpoPushToken(String expoPushToken);

  List<AlarmPushToken> findByUserIdAndEnabledTrue(Long userId);
}