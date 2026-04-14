package com.kgu.life_watch.domain.notification.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.notification.dto.AlarmGroupDto;
import com.kgu.life_watch.domain.notification.dto.MedicineAlarmRequest;
import com.kgu.life_watch.domain.notification.entity.AlarmGroup;
import com.kgu.life_watch.domain.notification.entity.MedicineAlarm;
import com.kgu.life_watch.domain.notification.repository.AlarmGroupRepository;
import com.kgu.life_watch.domain.notification.repository.MedicineAlarmRepository;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Service
@RequiredArgsConstructor
public class AlarmGroupService {
  private final AlarmGroupRepository alarmGroupRepository;
  private final MedicineAlarmRepository medicineAlarmRepository;

  // 알람 그룹 및 하위 알람 생성 -> 혹시 헷갈릴까봐 주석 달자믄
  // 하위 알람이 다른 약 알람이란 뜻이 아닌 그 약을 언제 복용할지를 각각 저장하는 개별 알림 데이터
  // 그룹은 약의 이름/주의사항/복용 주기 등을 묶은 거
  @Transactional
  public void createAlarmGroup(MedicineAlarmRequest request, User user) {
    AlarmGroup alarmGroup =
        AlarmGroup.builder()
            .user(user)
            .medicineName(request.medicineName())
            .medicineNote(request.medicineNote())
            .repeatCycle(MedicineAlarm.RepeatCycle.valueOf(request.repeatCycle()))
            .build();
    alarmGroupRepository.save(alarmGroup);

    for (int i = 0; i < request.times().size(); i++) {
      String timeStr = request.times().get(i);
      double dosage = request.dosage().get(i);

      LocalTime time = LocalTime.parse(timeStr);
      LocalDateTime alarmTime =
          LocalDateTime.now()
              .withHour(time.getHour())
              .withMinute(time.getMinute())
              .withSecond(0)
              .withNano(0);

      if (alarmTime.isBefore(LocalDateTime.now())) {
        alarmTime = alarmTime.plusDays(1);
      }

      MedicineAlarm alarm =
          MedicineAlarm.builder()
              .user(user)
              .medicineName(request.medicineName())
              .medicineNote(request.medicineNote())
              .repeatCycle(MedicineAlarm.RepeatCycle.valueOf(request.repeatCycle()))
              .time(alarmTime)
              .status(MedicineAlarm.AlarmStatus.SCHEDULED)
              .alarmGroup(alarmGroup)
              .dosage(dosage)
              .build();

      medicineAlarmRepository.save(alarm);
    }
  }

  // 기존 그룹에 알람 시간 추가 -> 기존 약이 더 먹을 일이 있을지는 잘 모르겠당당이
  @Transactional
  public void addAlarmToGroup(Long groupId, MedicineAlarmRequest request, User user) {
    AlarmGroup group =
        alarmGroupRepository
            .findById(groupId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.ALARM_NOT_FOUND));

    for (int i = 0; i < request.times().size(); i++) {
      String timeStr = request.times().get(i);
      double dosage = request.dosage().get(i);

      LocalTime time = LocalTime.parse(timeStr);
      LocalDateTime alarmTime =
          LocalDateTime.now()
              .withHour(time.getHour())
              .withMinute(time.getMinute())
              .withSecond(0)
              .withNano(0);

      if (alarmTime.isBefore(LocalDateTime.now())) {
        alarmTime = alarmTime.plusDays(1);
      }

      MedicineAlarm alarm =
          MedicineAlarm.builder()
              .user(user)
              .medicineName(request.medicineName())
              .medicineNote(request.medicineNote())
              .repeatCycle(MedicineAlarm.RepeatCycle.valueOf(request.repeatCycle()))
              .time(alarmTime)
              .status(MedicineAlarm.AlarmStatus.SCHEDULED)
              .alarmGroup(group)
              .dosage(dosage)
              .build();
      medicineAlarmRepository.save(alarm);
    }
  }

  // 알람 시간 및 주기 재설정 (기존 알람 제거 후 재생성)
  @Transactional
  public void rescheduleGroup(Long groupId, MedicineAlarmRequest request, User user) {
    AlarmGroup group =
        alarmGroupRepository
            .findById(groupId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.ALARM_NOT_FOUND));

    List<MedicineAlarm> alarms = medicineAlarmRepository.findAllByAlarmGroupId(groupId);
    medicineAlarmRepository.deleteAll(alarms);

    group.updateInfo(request.medicineName(), request.medicineNote());
    group.updateRepeatCycle(MedicineAlarm.RepeatCycle.valueOf(request.repeatCycle()));

    for (int i = 0; i < request.times().size(); i++) {
      String timeStr = request.times().get(i);
      double dosage = request.dosage().get(i);

      LocalTime time = LocalTime.parse(timeStr);
      LocalDateTime alarmTime =
          LocalDateTime.now()
              .withHour(time.getHour())
              .withMinute(time.getMinute())
              .withSecond(0)
              .withNano(0);

      if (alarmTime.isBefore(LocalDateTime.now())) {
        alarmTime = alarmTime.plusDays(1);
      }

      MedicineAlarm alarm =
          MedicineAlarm.builder()
              .user(user)
              .medicineName(request.medicineName())
              .medicineNote(request.medicineNote())
              .repeatCycle(MedicineAlarm.RepeatCycle.valueOf(request.repeatCycle()))
              .time(alarmTime)
              .status(MedicineAlarm.AlarmStatus.SCHEDULED)
              .alarmGroup(group)
              .dosage(dosage)
              .build();
      medicineAlarmRepository.save(alarm);
    }
  }

  // 그룹 내 전체 알람 완료 처리
  @Transactional
  public void markAllAlarmsInGroupComplete(Long groupId) {
    List<MedicineAlarm> alarms = medicineAlarmRepository.findAllByAlarmGroupId(groupId);
    for (MedicineAlarm alarm : alarms) {
      alarm.updateStatus(MedicineAlarm.AlarmStatus.COMPLETE);
    }
  }

  // 개별 알람 완료 처리
  @Transactional
  public void markAlarmAsCompleted(Long alarmId) {
    MedicineAlarm alarm =
        medicineAlarmRepository
            .findById(alarmId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.ALARM_NOT_FOUND));
    alarm.updateStatus(MedicineAlarm.AlarmStatus.COMPLETE);
  }

  // 반복 알람 처리용 - 다음 알람 예약
  public void createNextAlarm(MedicineAlarm current, LocalDateTime nextTime) {
    current.updateStatus(MedicineAlarm.AlarmStatus.COMPLETE);
    medicineAlarmRepository.save(current);

    MedicineAlarm nextAlarm =
        MedicineAlarm.builder()
            .user(current.getUser())
            .medicineName(current.getMedicineName())
            .medicineNote(current.getMedicineNote())
            .time(nextTime)
            .status(MedicineAlarm.AlarmStatus.SCHEDULED)
            .repeatCycle(current.getRepeatCycle())
            .alarmGroup(current.getAlarmGroup())
            .dosage(current.getDosage())
            .build();

    medicineAlarmRepository.save(nextAlarm);
  }

  // 사용자별 그룹 조회
  @Transactional(readOnly = true)
  public List<AlarmGroupDto> getAlarmGroups(User user) {
    List<AlarmGroup> groups = alarmGroupRepository.findAllByUser(user);
    return groups.stream()
        .map(
            group ->
                AlarmGroupDto.fromEntity(
                    group, medicineAlarmRepository.findAllByAlarmGroupId(group.getId())))
        .toList();
  }

  // 그룹 삭제
  @Transactional
  public void deleteGroup(Long groupId) {
    AlarmGroup group =
        alarmGroupRepository
            .findById(groupId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.ALARM_NOT_FOUND));
    alarmGroupRepository.delete(group);
  }

  // 그룹 이름/주의사항 수정
  @Transactional
  public void updateGroup(Long groupId, String newName, String newNote) {
    AlarmGroup group =
        alarmGroupRepository
            .findById(groupId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.ALARM_NOT_FOUND));
    group.updateInfo(newName, newNote);
  }
}
