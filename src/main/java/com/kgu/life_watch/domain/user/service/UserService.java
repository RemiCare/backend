package com.kgu.life_watch.domain.user.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.UserUpdateRequest;
import com.kgu.life_watch.domain.user.dto.request.ElderlyRegisterRequest;
import com.kgu.life_watch.domain.user.dto.request.WearableConnectionRequest;
import com.kgu.life_watch.domain.user.dto.response.ElderlySimpleInfoResponse;
import com.kgu.life_watch.domain.user.dto.response.UserProfileResponse;
import com.kgu.life_watch.domain.user.entity.ElderlyProfile;
import com.kgu.life_watch.domain.user.entity.ProtectorProfile;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.ElderlyProfileRepository;
import com.kgu.life_watch.domain.user.repository.ProtectorProfileRepository;
import com.kgu.life_watch.domain.user.repository.UserRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {
  private final UserRepository userRepository;
  private final ElderlyProfileRepository elderlyProfileRepository;
  private final ProtectorProfileRepository protectorProfileRepository;
  private final PasswordEncoder passwordEncoder;

  /** [어르신 추가 등록] 로그인한 보호자가 어르신 계정을 직접 생성하고 연결 */
  @Transactional
  public void registerElderly(User protectorUser, ElderlyRegisterRequest request) {
    // 중복 체크
    if (userRepository.existsByLoginId(request.loginId())) {
      throw LifelineException.from(ErrorCode.ACCOUNT_USERNAME_EXIST);
    }

    // 어르신 User 엔티티 생성
    User elderly =
        User.builder()
            .name(request.name())
            .loginId(request.loginId())
            .password(passwordEncoder.encode(request.password()))
            .phoneNumber(request.phoneNumber())
            .address(request.address())
            .rrn(request.rrn())
            .birthDate(request.birthDate())
            .gender(request.gender())
            .role(User.Role.ELDER)
            .fcmToken(request.fcmToken())
            .loginCode(generateUniqueLoginCode())
            .build();
    userRepository.save(elderly);

    // 보호자 프로필 조회 및 어르신 프로필 생성/연결
    ProtectorProfile protectorProfile =
        protectorProfileRepository
            .findByUser(protectorUser)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    ElderlyProfile elderlyProfile =
        ElderlyProfile.builder()
            .user(elderly)
            .protectorProfile(protectorProfile)
            .drn(request.drn())
            .protectorName(request.protectorName())
            .protectorContact(request.protectorContact())
            .build();
    elderlyProfileRepository.save(elderlyProfile);
  }

  /** [어르신 등록 해제] 계정 삭제가 아닌 보호자와의 연결만 끊음 */
  @Transactional
  public void removeElderly(User protectorUser, Long elderlyId) {
    ElderlyProfile elderlyProfile =
        elderlyProfileRepository
            .findByUserId(elderlyId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    // 해당 어르신을 담당하는 보호자가 본인이 맞는지 확인
    if (!elderlyProfile.getProtectorProfile().getUser().getId().equals(protectorUser.getId())) {
      throw LifelineException.from(ErrorCode.INVALID_REQUEST);
    }

    // 연결 해제
    elderlyProfile.setProtectorProfile(null);
  }

  /** [관리 중인 어르신 목록 조회] 사회복지사 로직에서 보호자 로직으로 변경 */
  public List<ElderlySimpleInfoResponse> getMyElderlyList(User protectorUser) {
    ProtectorProfile protectorProfile =
        protectorProfileRepository
            .findByUser(protectorUser)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    return elderlyProfileRepository.findAllByProtectorProfileId(protectorProfile.getId()).stream()
        .map(profile -> ElderlySimpleInfoResponse.from(profile.getUser()))
        .toList();
  }

  /** 내 프로필 정보 조회 */
  public UserProfileResponse getProfile(User user) {
    return UserProfileResponse.toDto(user);
  }

  /** 사용자 정보 수정 (에러 수정: 인스턴스 메서드로 정의) */
  @Transactional
  public void updateUserInfo(Long userId, UserUpdateRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    user.updateBasicInfo(request.name(), request.phoneNumber(), request.address());

    if (user.getRole() == User.Role.ELDER && user.getElderlyProfile() != null) {
      user.getElderlyProfile().updateProtector(request.protectorName(), request.protectorContact());
    }
  }

  /** 웨어러블 기기 연결 상태 업데이트 (에러 수정) */
  @Transactional
  public void updateWearableConnectionStatus(Long userId, WearableConnectionRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    ElderlyProfile profile = user.getElderlyProfile();
    if (profile == null) {
      throw LifelineException.from(ErrorCode.MEMBER_NOT_FOUND);
    }

    profile.updateWearableConnection(request.isConnected(), request.deviceName());
  }

  private String generateUniqueLoginCode() {
    SecureRandom random = new SecureRandom();
    String code;
    do {
      code = "RC-" + (random.nextInt(9000) + 1000);
    } while (userRepository.findByLoginCode(code).isPresent());
    return code;
  }
}
