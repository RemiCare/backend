package com.kgu.life_watch.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.user.entity.ElderlyProfile;

public interface ElderlyProfileRepository extends JpaRepository<ElderlyProfile, Long> {
  // @EntityGraph는 LAZY 전략을 유지하면서도 필요한 순간에만 명시적으로 fetch join을 해주는 방식
  // JPA가 쿼리를 생성할 때 연관된 엔티티까지 조인해서 한 번에 로딩 -> 이거 안하면 알람 보낼 때 트랜잭션 범위를 벗어난 시점에서 Lazy 로딩을 시도함 정신 나갈뻔
  @EntityGraph(attributePaths = "protectorProfile")
  Optional<ElderlyProfile> findWithProtectorProfileById(Long id);

  List<ElderlyProfile> findAllByProtectorProfileIsNull();

  List<ElderlyProfile> findAllByProtectorProfileId(Long protectorProfileId);

  // 노인의 userId를 기반으로 ElderlyProfile과 그에 연결된 ProtectorProfile과 User까지 함께 로딩
  @EntityGraph(attributePaths = {"protectorProfile.user"})
  Optional<ElderlyProfile> findByUserId(Long userId);
}
