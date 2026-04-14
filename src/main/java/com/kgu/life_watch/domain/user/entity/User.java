package com.kgu.life_watch.domain.user.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

import com.kgu.life_watch.global.domain.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String loginId;

  @Column(nullable = true) // null 값 허용해달라고 해서 수정
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String phoneNumber;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false, name = "resident_registration_number")
  private String rrn;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Column(nullable = false)
  private String gender;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(name = "fcm_token")
  private String fcmToken;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private ElderlyProfile elderlyProfile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
  private SocialWorkerProfile socialWorkerProfile;

  public enum Role {
    USER,
    ADMIN,
    SOCIAL_WORKER
  }

  public void setElderlyProfile(ElderlyProfile elderlyProfile) {
    this.elderlyProfile = elderlyProfile;
    elderlyProfile.setUser(this);
  }

  public void setSocialWorkerProfile(SocialWorkerProfile socialWorkerProfile) {
    this.socialWorkerProfile = socialWorkerProfile;
    socialWorkerProfile.setUser(this);
  }

  public void changePassword(String newPassword) {
    this.password = newPassword;
  }

  // FCM 토큰 업데이트 메서드
  public void updateFcmToken(String token) {
    this.fcmToken = token;
  }

  // 유저 정보 수정 메서드
  public void updateBasicInfo(String name, String phoneNumber, String address) {
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.address = address;
  }
}
