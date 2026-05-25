package com.kgu.life_watch.domain.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.kgu.life_watch.domain.notification.entity.MedicineAlarm;
import com.kgu.life_watch.domain.notification.entity.NotificationLog;
import com.kgu.life_watch.domain.notification.repository.NotificationLogRepository;
import com.kgu.life_watch.domain.user.entity.ElderlyProfile;
import com.kgu.life_watch.domain.user.entity.ProtectorProfile;
import com.kgu.life_watch.domain.user.repository.ElderlyProfileRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseMessageService {

  private final ElderlyProfileRepository elderlyProfileRepository;
  private final NotificationLogRepository notificationLogRepository;

  @Transactional
  public void sendEmergencyAlert(Long elderlyId, String label, String explanation) {
    // 정상일 경우 아무 작업도 하지 않음
    if (!isAbnormal(label)) {
      return;
    }

    ElderlyProfile elderly =
        elderlyProfileRepository
            .findByUserId(elderlyId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    String name = elderly.getUser().getName();
    String title = "응급상황 발생 - " + name;
    String body = "응급상황이 감지되었습니다.\n" + explanation;

    // 1. 보호자에게 알림 전송
    ProtectorProfile protector = elderly.getProtectorProfile();
    if (protector != null && protector.getUser().getFcmToken() != null) {
      Message protectorMessage =
          Message.builder()
              .putData("title", title)
              .putData("body", "대상자: " + name + "\n" + body)
              .putData("elderlyId", String.valueOf(elderly.getUser().getId()))
              .putData("elderlyName", name)
              .setToken(protector.getUser().getFcmToken())
              .build();
      try {
        FirebaseMessaging.getInstance().send(protectorMessage);
      } catch (FirebaseMessagingException e) {
        log.error("보호자에게 FCM 전송 실패: {}", e.getMessage());
      }
    }

    // 2. 노인 본인(워치/앱)에게 알림 전송
    if (elderly.getUser().getFcmToken() != null) {
      Message elderlyMessage =
          Message.builder()
              .putData("title", "응급 감지 알림")
              .putData("body", "도움이 필요하신가요?\n" + explanation)
              .setNotification(
                  Notification.builder()
                      .setTitle("응급 감지 알림")
                      .setBody("도움이 필요하신가요?\n" + explanation)
                      .build())
              .setToken(elderly.getUser().getFcmToken())
              .build();
      try {
        FirebaseMessaging.getInstance().send(elderlyMessage);
      } catch (FirebaseMessagingException e) {
        log.error("노인 본인에게 FCM 전송 실패: {}", e.getMessage());
      }
    }

    notificationLogRepository.save(NotificationLog.of(elderlyId, label, explanation));
  }

  private boolean isAbnormal(String label) {
    return label != null && label.contains("Emergency (비정상)");
  }

  @Transactional
  public void sendMedicineAlarm(MedicineAlarm alarm) {
    String elderlyFcm = alarm.getUser().getFcmToken();

    String baseBody =
        "약 복용 정보\n"
            + "약 이름: "
            + alarm.getMedicineName()
            + "\n"
            + "복용 시간: "
            + alarm.getTime()
            + "\n"
            + "복용량: "
            + alarm.getDosage()
            + "알"
            + (alarm.getMedicineNote() != null ? "\n주의사항: " + alarm.getMedicineNote() : "");

    // 노인에게 알림 (앱용 → setNotification 사용)
    if (elderlyFcm != null) {
      Message elderlyMessage =
          Message.builder()
              .putData("title", "약 복용 알림")
              .putData("body", baseBody)
              .setNotification(Notification.builder().setTitle("약 복용 알림").setBody(baseBody).build())
              .setToken(elderlyFcm)
              .build();

      try {
        FirebaseMessaging.getInstance().send(elderlyMessage);
      } catch (FirebaseMessagingException e) {
        throw LifelineException.from(ErrorCode.FCM_SEND_FAILED);
      }
    }

    ElderlyProfile elderly =
        elderlyProfileRepository
            .findByUserId(alarm.getUser().getId())
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    ProtectorProfile protector = elderly.getProtectorProfile();
    if (protector == null
        || protector.getUser() == null
        || protector.getUser().getFcmToken() == null) {
      // 보호자 토큰이 없어도 노인에게는 보냈으므로 로그만 남김
      log.warn("보호자 FCM 토큰이 없어 알람을 보내지 못했습니다.");
      return;
    }

    String name = elderly.getUser().getName();
    String protectorFcm = protector.getUser().getFcmToken();
    String protectorBody = "대상자: " + name + "\n" + "약 복용 알림\n" + baseBody;

    Message protectorMessage =
        Message.builder()
            .putData("title", "보호 대상자 약 복용 알림")
            .putData("body", protectorBody)
            .putData("elderlyId", String.valueOf(elderly.getUser().getId()))
            .putData("elderlyName", name)
            .setToken(protectorFcm)
            .build();

    try {
      FirebaseMessaging.getInstance().send(protectorMessage);
    } catch (FirebaseMessagingException e) {
      log.error("보호자에게 약 복용 알림 FCM 전송 실패: {}", e.getMessage());
    }
  }
}
