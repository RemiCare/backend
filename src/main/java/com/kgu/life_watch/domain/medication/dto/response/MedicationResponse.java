package com.kgu.life_watch.domain.medication.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import com.kgu.life_watch.domain.medication.entity.Medication;

public record MedicationResponse(
    Long id, String name, LocalTime dosageTime, List<DayOfWeek> daysOfWeek, boolean isTaken) {
  public static MedicationResponse from(Medication medication) {
    return new MedicationResponse(
        medication.getId(),
        medication.getName(),
        medication.getDosageTime(),
        medication.getDaysOfWeek(),
        medication.isTaken());
  }
}
