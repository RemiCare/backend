package com.kgu.life_watch.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.user.entity.ElderlyProfile;

public interface ElderlyProfileRepository extends JpaRepository<ElderlyProfile, Long> {

  // 보호자 ID로 관리 중인 어르신 목록 전체 조회
  List<ElderlyProfile> findAllByProtectorProfileId(Long protectorProfileId);

  // 어르신 연결 해제 및 정보 수정 시 사용
  // @EntityGraph를 유지하여 연관된 엔티티까지 한 번에 로딩함으로써 Lazy 로딩 에러 방지
  @EntityGraph(attributePaths = {"protectorProfile.user"})
  Optional<ElderlyProfile> findByUserId(Long userId);
}
