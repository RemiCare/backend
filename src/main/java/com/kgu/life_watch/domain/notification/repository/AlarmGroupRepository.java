package com.kgu.life_watch.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.notification.entity.AlarmGroup;
import com.kgu.life_watch.domain.user.entity.User;

public interface AlarmGroupRepository extends JpaRepository<AlarmGroup, Long> {
  List<AlarmGroup> findAllByUser(User user);
}
