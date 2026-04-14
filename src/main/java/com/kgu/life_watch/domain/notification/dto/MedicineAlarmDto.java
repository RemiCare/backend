package com.kgu.life_watch.domain.notification.dto;

import java.time.LocalDateTime;

import com.kgu.life_watch.domain.notification.entity.MedicineAlarm;
import com.kgu.life_watch.domain.notification.entity.MedicineAlarm.AlarmStatus;

public record MedicineAlarmDto(
    Long alarmId,
    Long elderlyId,
    String medicineName,
    LocalDateTime time,
    String medicineNote,
    boolean completed,
    String repeatCycle,
    double dosage) {

  public static MedicineAlarmDto fromEntity(MedicineAlarm alarm) {
    return new MedicineAlarmDto(
        alarm.getId(),
        alarm.getUser().getId(),
        alarm.getMedicineName(),
        alarm.getTime(),
        alarm.getMedicineNote(),
        alarm.getStatus() == AlarmStatus.COMPLETE,
        alarm.getRepeatCycle().name(),
        alarm.getDosage());
  }
}
