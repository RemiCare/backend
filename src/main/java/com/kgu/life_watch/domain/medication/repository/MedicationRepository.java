package com.kgu.life_watch.domain.medication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.medication.entity.Medication;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
  //  특정 어르신의 약 목록을 조회
  List<Medication> findAllByElderlyProfileId(Long elderlyProfileId);
}
