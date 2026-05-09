package com.kgu.life_watch.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.user.entity.ProtectorProfile;
import com.kgu.life_watch.domain.user.entity.User;

public interface ProtectorProfileRepository extends JpaRepository<ProtectorProfile, Long> {

  /** User 엔티티를 통해 해당 사용자의 보호자 프로필을 조회합니다. */
  Optional<ProtectorProfile> findByUser(User user);

  /** User의 ID를 통해 보호자 프로필이 존재하는지 확인합니다. */
  boolean existsByUserId(Long userId);
}
