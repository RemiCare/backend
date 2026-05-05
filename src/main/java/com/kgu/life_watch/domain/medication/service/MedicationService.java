package com.kgu.life_watch.domain.medication.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.medication.dto.request.MedicationCreateRequest;
import com.kgu.life_watch.domain.medication.entity.Medication;
import com.kgu.life_watch.domain.medication.repository.MedicationRepository;
import com.kgu.life_watch.domain.user.repository.ElderlyProfileRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MedicationService {
  private final MedicationRepository medicationRepository;
  private final ElderlyProfileRepository elderlyProfileRepository;

  // 어르신별 복약 목록 조회
  public List<Medication> getMedicationList(Long elderlyProfileId) {
    return medicationRepository.findAllByElderlyProfileId(elderlyProfileId);
  }

  // 복약 상태 변경 (어르신 확인 혹은 보호자 수동 설정)
  @Transactional
  public void updateMedicationStatus(Long medicationId, boolean isTaken) {
    Medication medication =
        medicationRepository
            .findById(medicationId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEDICATION_NOT_FOUND));

    medication.updateTakenStatus(isTaken);
  }

  @Transactional
  public void createMedication(MedicationCreateRequest request) {
    com.kgu.life_watch.domain.user.entity.ElderlyProfile elderly =
        elderlyProfileRepository
            .findById(request.elderlyId())
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    Medication medication =
        Medication.createMedication(
            request.name(), request.dosageTime(), request.daysOfWeek(), elderly);
    medicationRepository.save(medication);
  }
}
