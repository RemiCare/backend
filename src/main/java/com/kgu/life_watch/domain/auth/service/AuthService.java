package com.kgu.life_watch.domain.auth.service;

import java.security.SecureRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.*;
import com.kgu.life_watch.domain.auth.dto.response.*;
import com.kgu.life_watch.domain.user.entity.ElderlyProfile;
import com.kgu.life_watch.domain.user.entity.ProtectorProfile;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.ElderlyProfileRepository;
import com.kgu.life_watch.domain.user.repository.ProtectorProfileRepository;
import com.kgu.life_watch.domain.user.repository.UserRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;
import com.kgu.life_watch.global.jwt.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final ElderlyProfileRepository elderlyProfileRepository;
  private final ProtectorProfileRepository protectorProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthSmsService authSmsService;

  @Transactional
  public void signUpElderly(ElderlySignUpRequest request) {
    validateDuplicateUser(request.loginId(), request.phoneNumber());

    User user =
        User.builder()
            .name(request.name())
            .loginId(request.loginId())
            .email(request.email())
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

    ElderlyProfile.ElderlyProfileBuilder profileBuilder =
        ElderlyProfile.builder()
            .user(user)
            .drn(request.drn())
            .protectorContact(request.protectorContact())
            .protectorName(request.protectorName());

    if (request.protectorId() != null && !request.protectorId().isBlank()) {
      mapProtectorToElderly(profileBuilder, request.protectorId());
    }

    elderlyProfileRepository.save(profileBuilder.build());
  }

  @Transactional
  public void signUpProtector(ProtectorSignUpRequest request) {
    validateDuplicateUser(request.loginId(), request.phoneNumber());

    User user =
        User.builder()
            .name(request.name())
            .loginId(request.loginId())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .phoneNumber(request.phoneNumber())
            .address(request.address())
            .rrn(request.rrn())
            .birthDate(request.birthDate())
            .gender(request.gender())
            .role(User.Role.PROTECTOR)
            .fcmToken(request.fcmToken())
            .build();

    ProtectorProfile profile = ProtectorProfile.builder().user(user).build();
    protectorProfileRepository.save(profile);
  }

  // RC-XXXX 형식의 유니크한 코드 생성
  private String generateUniqueLoginCode() {
    SecureRandom random = new SecureRandom();
    String code;
    do {
      int num = random.nextInt(9000) + 1000; // 1000 ~ 9999
      code = "RC-" + num;
    } while (userRepository.findByLoginCode(code).isPresent());
    return code;
  }

  // Helper Methods

  private void validateDuplicateUser(String loginId, String phoneNumber) {
    if (userRepository.existsByLoginId(loginId)) {
      throw LifelineException.from(ErrorCode.ACCOUNT_USERNAME_EXIST);
    }
    if (userRepository.existsByPhoneNumber(phoneNumber)) {
      throw LifelineException.from(ErrorCode.DUPLICATE_PHONE_NUMBER);
    }
  }

  private void mapProtectorToElderly(
      ElderlyProfile.ElderlyProfileBuilder builder, String protectorIdStr) {
    try {
      Long protectorId = Long.valueOf(protectorIdStr);
      ProtectorProfile protector =
          protectorProfileRepository
              .findById(protectorId)
              .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
      builder.protectorProfile(protector);
    } catch (NumberFormatException e) {
      throw LifelineException.from(ErrorCode.INVALID_REQUEST);
    }
  }

  /** 공통 로그인 (ID/PW) */
  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByLoginId(request.loginId())
            .orElseThrow(() -> LifelineException.from(ErrorCode.INCORRECT_ACCOUNT));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw LifelineException.from(ErrorCode.INCORRECT_PASSWORD);
    }

    return createLoginResponse(user);
  }

  /** 노인 전용 로그인 (코드 인증) */
  @Transactional(readOnly = true)
  public LoginResponse loginByCode(String loginCode) {
    User user =
        userRepository
            .findByLoginCode(loginCode)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    return createLoginResponse(user);
  }

  /**
   * 역할별 맞춤 로그인 응답 생성 보호자일 경우: 담당하고 있는 첫 번째 어르신의 정보를 포함 어르신일 경우: 프로필에 등록된 수동 입력 보호자 정보 혹은 시스템 연동 보호자
   * 정보 포함
   */
  private LoginResponse createLoginResponse(User user) {
    String jwt = jwtTokenProvider.generateToken(user);
    boolean isProtector = user.getRole() == User.Role.PROTECTOR;
    String roleStr = isProtector ? "caregiver" : "elder";

    String assignedElderName = null;
    String assignedElderCode = null;

    // 보호자인 경우 연결된 첫 번째 어르신 정보 가져오기
    if (isProtector && user.getProtectorProfile() != null) {
      var seniors = user.getProtectorProfile().getAssignedSeniors();
      if (!seniors.isEmpty()) {
        ElderlyProfile primaryElder = seniors.get(0);
        assignedElderName = primaryElder.getUser().getName();
        assignedElderCode = primaryElder.getUser().getLoginCode();
      }
    }

    if (user.getRole() == User.Role.ELDER && user.getElderlyProfile() != null) {
      ElderlyProfile elderly = user.getElderlyProfile();
      ProtectorProfile protector = elderly.getProtectorProfile();

      String protectorName =
          (protector != null && protector.getUser() != null) ? protector.getUser().getName() : null;
      String protectorPhone =
          (protector != null && protector.getUser() != null)
              ? protector.getUser().getPhoneNumber()
              : null;

      return new LoginResponse(
          user.getName(),
          jwt,
          user.getBirthDate(),
          elderly.getProtectorName(),
          elderly.getProtectorContact(),
          protectorName,
          protectorPhone,
          user.getId(),
          false,
          roleStr,
          user.getLoginCode(),
          user.getPhoneNumber(),
          user.getAddress(),
          null,
          null);
    }

    return new LoginResponse(
        user.getName(),
        jwt,
        user.getBirthDate(),
        null,
        null,
        null,
        null,
        user.getId(),
        isProtector,
        roleStr,
        user.getLoginCode(),
        user.getPhoneNumber(),
        user.getAddress(),
        assignedElderName,
        assignedElderCode);
  }

  /** 사용자 ID로 엔티티를 조회하여 기기 식별용 FCM 토큰을 최신화 */
  @Transactional
  public void updateFcmToken(Long userId, String fcmToken) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    user.updateFcmToken(fcmToken);
  }

  /** 회원가입 폼 입력 시 실시간으로 아이디나 전화번호 사용 가능 여부를 체크 */
  @Transactional(readOnly = true)
  public void checkDuplicate(String loginId, String phoneNumber) {
    if (loginId != null && !loginId.isBlank()) {
      if (userRepository.existsByLoginId(loginId)) {
        throw LifelineException.from(ErrorCode.ACCOUNT_USERNAME_EXIST);
      }
    }
    if (phoneNumber != null && !phoneNumber.isBlank()) {
      if (userRepository.existsByPhoneNumber(phoneNumber)) {
        throw LifelineException.from(ErrorCode.DUPLICATE_PHONE_NUMBER);
      }
    }
  }

  /** 아이디와 전화번호 쌍이 일치하는 사용자가 있는지 확인하여 본인 여부 검증 */
  @Transactional(readOnly = true)
  public void verifyIdentity(String loginId, String phoneNumber) {
    userRepository
        .findByLoginIdAndPhoneNumber(loginId, phoneNumber)
        .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
  }
}
