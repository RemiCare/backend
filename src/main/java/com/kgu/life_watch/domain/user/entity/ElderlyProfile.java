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

  @Column(nullable = true, name = "disability_registration_number")
  private String drn; // 장애등록번호 선택

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "protector_id", referencedColumnName = "user_id")
  private ProtectorProfile protectorProfile;

  @Column(nullable = false)
  private String protectorContact; // 보호자 연락처 필수

  @Column(nullable = false, name = "protector_name")
  private String protectorName; // 보호자 이름 넣어달라고해서 추가

  @Column(nullable = false)
  @Builder.Default
  private boolean isConnected = false;

  @Column(nullable = true)
  private String deviceName;

  public void setUser(User user) {
    this.user = user;
  }

  public void setProtectorProfile(ProtectorProfile profile) {
    this.protectorProfile = profile;
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

  public void updateWearableConnection(boolean isConnected, String deviceName) {
    this.isConnected = isConnected;
    this.deviceName = deviceName;
  }
}
