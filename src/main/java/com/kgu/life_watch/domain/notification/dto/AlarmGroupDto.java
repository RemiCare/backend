package com.kgu.life_watch.domain.notification.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.kgu.life_watch.domain.notification.entity.AlarmGroup;
import com.kgu.life_watch.domain.notification.entity.MedicineAlarm;

public record AlarmGroupDto(
    Long groupId,
    String medicineName,
    String medicineNote,
    String repeatCycle,
    List<MedicineAlarmDto> alarms) {
  public static AlarmGroupDto fromEntity(AlarmGroup group, List<MedicineAlarm> alarmList) {
    return new AlarmGroupDto(
        group.getId(),
        group.getMedicineName(),
        group.getMedicineNote(),
        group.getRepeatCycle().name(),
        alarmList.stream().map(MedicineAlarmDto::fromEntity).collect(Collectors.toList()));
  }
}
