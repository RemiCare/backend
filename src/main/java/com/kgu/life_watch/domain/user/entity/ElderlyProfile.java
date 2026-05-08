package com.kgu.life_watch.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ElderlyProfile {

  @Id private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false, name = "disability_registration_number")
  private String drn; // 장애등록번호 필수

  // SocialWorkerProfile에 assignedElderId (단일 Long) -> 여러 노인과 매핑하는 구조로 바꿔야 함.
  // 즉, 1명의 사회복지사가 여러 명의 노인을 담당할 수 있는 구조로 변경해야 함.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "social_worker_id", referencedColumnName = "user_id")
  private SocialWorkerProfile socialWorkerProfile;

  @Column(nullable = false)
  private String protectorContact; // 보호자 연락처 필수

  @Column(nullable = false, name = "protector_name")
  private String protectorName; // 보호자 이름 넣어달라고해서 추가

  public void setUser(User user) {
    this.user = user;
  }

  public void setSocialWorkerProfile(SocialWorkerProfile profile) {
    this.socialWorkerProfile = profile;
  }

  // 노인용 추가 정보 수정 메서드
  public void updateProtector(String protectorName, String protectorContact) {
    if (protectorName != null) {
      this.protectorName = protectorName;
    }
    if (protectorContact != null) {
      this.protectorContact = protectorContact;
    }
  }

  // 웨어러블 연결 상태 ( 기본값 :false )
  @Column(name = "is_wearable_connected")
  @Builder.Default
  private boolean isWearableConnected = false;

  // 연결된 웨어러블 기기 이름 (어떤 워치인지 표시)
  @Column(name = "wearable_device_name")
  private String wearableDeviceName;

  // 웨어러블 연결 상태를 변경하는 비즈니스 메서드
  public void updateWearableConnection(boolean isConnected, String deviceName) {
    this.isWearableConnected = isConnected;
    this.wearableDeviceName = deviceName;
  }
}
