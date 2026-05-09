package com.kgu.life_watch.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
  // loginId로 유저 존재 여부 확인
  boolean existsByLoginId(String loginId);

  // phoneNumber로 유저 존재 여부 확인
  boolean existsByPhoneNumber(String phoneNumber);

  // loginId로 유저 조회
  @EntityGraph(
      attributePaths = {
        "elderlyProfile",
        "elderlyProfile.protectorProfile",
        "elderlyProfile.protectorProfile.user",
        "protectorProfile"
      })
  Optional<User> findByLoginId(String loginId);

  // name + phoneNumber로 조회
  Optional<User> findByNameAndPhoneNumber(String name, String phoneNumber);

  // loginId + phoneNumber로 조회
  Optional<User> findByLoginIdAndPhoneNumber(String loginId, String phoneNumber);

  // loginCode로 유저 조회
  @EntityGraph(
      attributePaths = {"elderlyProfile", "elderlyProfile.protectorProfile", "protectorProfile"})
  Optional<User> findByLoginCode(String loginCode);
}
