package com.kgu.life_watch.domain.health.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.health.entity.HealthData;

public interface HealthDataRepository extends JpaRepository<HealthData, Long> {

  Optional<HealthData> findTopByUserIdOrderByMeasuredAtDesc(Long userId);

  List<HealthData> findAllByUserIdOrderByMeasuredAtDesc(Long userId);

  List<HealthData> findAllByUserIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
      Long userId,
      LocalDateTime from,
      LocalDateTime to
  );

  List<HealthData> findAllByUserIdAndMeasuredAtAfterOrderByMeasuredAtDesc(
      Long userId,
      LocalDateTime from
  );

  List<HealthData> findAllByUserIdAndMeasuredAtBeforeOrderByMeasuredAtDesc(
      Long userId,
      LocalDateTime to
  );
}