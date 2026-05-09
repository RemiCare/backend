package com.kgu.life_watch.domain.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.UserUpdateRequest;
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

  /** 노인 할당 (보호자 - 노인 연결) */
  @Transactional
  public void assignElderly(Long elderlyId, Long protectorId) {
    ElderlyProfile elderly = findElderlyById(elderlyId);
    ProtectorProfile protector = findProtectorById(protectorId);
    protector.addElderly(elderly);
  }

  /** 노인 할당 해제 */
  @Transactional
  public void unassignElderly(Long elderlyId, Long protectorId) {
    ElderlyProfile elderly = findElderlyById(elderlyId);
    ProtectorProfile protector = findProtectorById(protectorId);

    protector.getAssignedSeniors().remove(elderly);
    elderly.setProtectorProfile(null);
  }

  /** 내 프로필 정보 조회 */
  public UserProfileResponse getProfile(User user) {
    return UserProfileResponse.toDto(user);
  }

  /** 사용자 정보 수정 (공통 정보 + 노인인 경우 보호자 정보) */
  @Transactional
  public void updateUserInfo(Long userId, UserUpdateRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    // 기본 정보(이름, 번호, 주소) 업데이트
    user.updateBasicInfo(request.name(), request.phoneNumber(), request.address());

    // 노인 역할인 경우 추가 정보 업데이트
    if (user.getRole() == User.Role.ELDER && user.getElderlyProfile() != null) {
      user.getElderlyProfile().updateProtector(request.protectorName(), request.protectorContact());
    }
  }

  /** 할당 가능한 노인 목록 조회 (보호자가 없는 노인들) */
  public List<ElderlySimpleInfoResponse> getAssignableElderlyList() {
    return elderlyProfileRepository.findAllByProtectorProfileIsNull().stream()
        .map(profile -> ElderlySimpleInfoResponse.from(profile.getUser()))
        .toList();
  }

  /** 특정 보호자가 담당 중인 노인 목록 조회 */
  public List<ElderlySimpleInfoResponse> getAssignedElderlyList(User protectorUser) {
    Long protectorProfileId = protectorUser.getProtectorProfile().getId();
    return elderlyProfileRepository.findAllByProtectorProfileId(protectorProfileId).stream()
        .map(profile -> ElderlySimpleInfoResponse.from(profile.getUser()))
        .toList();
  }

  /** 웨어러블 기기 연결 상태 업데이트 */
  @Transactional
  public void updateWearableConnectionStatus(Long userId, WearableConnectionRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    ElderlyProfile profile = user.getElderlyProfile();
    if (profile == null) {
      throw LifelineException.from(ErrorCode.MEMBER_NOT_FOUND); // 노인 프로필 없음 에러
    }

    profile.updateWearableConnection(request.isConnected(), request.deviceName());
  }

  // Helper Methods

  private ElderlyProfile findElderlyById(Long id) {
    return elderlyProfileRepository
        .findById(id)
        .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
  }

  private ProtectorProfile findProtectorById(Long id) {
    return protectorProfileRepository
        .findById(id)
        .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
  }
}
