package com.kgu.life_watch.domain.health.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.health.entity.HealthData;

public interface HealthDataRepository extends JpaRepository<HealthData, Long> {

  Optional<HealthData> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

  List<HealthData> findByUserIdOrderByRecordDateDesc(Long userId);
}
