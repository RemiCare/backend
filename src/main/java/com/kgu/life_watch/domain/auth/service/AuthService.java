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

  /** [통합 회원가입] 보호자 계정 생성 -> 보호자 프로필 생성 -> 어르신 계정 생성 -> 보호자-어르신 연결 프로필 생성 */
  @Transactional
  public CombinedSignUpResponse signUpCombined(CombinedSignUpRequest request) { // 보호자 중복 체크 및 계정 생성
    User protectorUser = createProtectorUser(request.protector());
    userRepository.save(protectorUser);

    // 보호자 프로필 생성
    ProtectorProfile protectorProfile = ProtectorProfile.builder().user(protectorUser).build();
    protectorProfileRepository.save(protectorProfile);

    // 어르신 중복 체크 및 계정 생성
    User elderlyUser = createElderlyUser(request.elderly());
    userRepository.save(elderlyUser);

    // 어르신 프로필 생성 및 방금 생성된 보호자 프로필과 매핑
    ElderlyProfile elderlyProfile =
        ElderlyProfile.builder()
            .user(elderlyUser)
            .protectorProfile(protectorProfile)
            .drn(request.elderly().drn())
            .protectorContact(request.elderly().protectorContact())
            .protectorName(request.elderly().protectorName())
            .build();
    elderlyProfileRepository.save(elderlyProfile);

    return new CombinedSignUpResponse(
        protectorUser.getName(), elderlyUser.getName(), elderlyUser.getLoginCode() // RC-XXXX 코드 반환
        );
  }

  private User createProtectorUser(ProtectorSignUpRequest req) {
    checkDuplicate(req.loginId(), req.phoneNumber());
    return User.builder()
        .name(req.name())
        .loginId(req.loginId())
        .email(req.email())
        .password(passwordEncoder.encode(req.password()))
        .phoneNumber(req.phoneNumber())
        .address(req.address())
        .rrn(req.rrn())
        .birthDate(req.birthDate())
        .gender(req.gender())
        .role(User.Role.PROTECTOR)
        .fcmToken(req.fcmToken())
        .build();
  }

  private User createElderlyUser(ElderlySignUpRequest req) {
    checkDuplicate(req.loginId(), req.phoneNumber());
    return User.builder()
        .name(req.name())
        .loginId(req.loginId())
        .email(req.email())
        .password(passwordEncoder.encode(req.password()))
        .phoneNumber(req.phoneNumber())
        .address(req.address())
        .rrn(req.rrn())
        .birthDate(req.birthDate())
        .gender(req.gender())
        .role(User.Role.ELDER)
        .fcmToken(req.fcmToken())
        .loginCode(generateUniqueLoginCode())
        .build();
  }

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

  @Transactional(readOnly = true)
  public LoginResponse loginByCode(String loginCode) {
    User user =
        userRepository
            .findByLoginCode(loginCode)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
    return createLoginResponse(user);
  }

  @Transactional
  public void updateFcmToken(Long userId, String fcmToken) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
    user.updateFcmToken(fcmToken);
  }

  @Transactional(readOnly = true)
  public void checkDuplicate(String loginId, String phoneNumber) {
    if (loginId != null && !loginId.isBlank() && userRepository.existsByLoginId(loginId)) {
      throw LifelineException.from(ErrorCode.ACCOUNT_USERNAME_EXIST);
    }
    if (phoneNumber != null
        && !phoneNumber.isBlank()
        && userRepository.existsByPhoneNumber(phoneNumber)) {
      throw LifelineException.from(ErrorCode.DUPLICATE_PHONE_NUMBER);
    }
  }

  @Transactional(readOnly = true)
  public void verifyIdentity(String loginId, String phoneNumber) {
    userRepository
        .findByLoginIdAndPhoneNumber(loginId, phoneNumber)
        .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
  }

  /** [로그인 응답 생성] 역할별(보호자/어르신) 맞춤형 데이터를 포함한 응답 객체를 빌드 */
  private LoginResponse createLoginResponse(User user) {
    String jwt = jwtTokenProvider.generateToken(user);
    boolean isProtector = user.getRole() == User.Role.PROTECTOR;
    String roleStr = isProtector ? "caregiver" : "elder";

    String assignedElderName = null;
    String assignedElderCode = null;

    // 보호자인 경우: 담당하고 있는 첫 번째 어르신 정보 조회
    if (isProtector && user.getProtectorProfile() != null) {
      var seniors = user.getProtectorProfile().getAssignedSeniors();
      if (seniors != null && !seniors.isEmpty()) {
        ElderlyProfile primaryElder = seniors.get(0);
        assignedElderName = primaryElder.getUser().getName();
        assignedElderCode = primaryElder.getUser().getLoginCode();
      }
    }

    // 어르신인 경우: 프로필 및 연동된 보호자 정보 포함
    if (user.getRole() == User.Role.ELDER && user.getElderlyProfile() != null) {
      ElderlyProfile elderly = user.getElderlyProfile();
      ProtectorProfile protector = elderly.getProtectorProfile();

      String systemProtectorName =
          (protector != null && protector.getUser() != null) ? protector.getUser().getName() : null;
      String systemProtectorPhone =
          (protector != null && protector.getUser() != null)
              ? protector.getUser().getPhoneNumber()
              : null;

      return new LoginResponse(
          user.getName(),
          jwt,
          user.getBirthDate(),
          elderly.getProtectorName(), // 수동 입력 보호자명
          elderly.getProtectorContact(), // 수동 입력 보호자 연락처
          systemProtectorName,
          systemProtectorPhone, // 시스템 연동 보호자 정보
          user.getId(),
          false,
          roleStr,
          user.getLoginCode(),
          user.getPhoneNumber(),
          user.getAddress(),
          null,
          null);
    }

    // 보호자 혹은 프로필 없는 경우 기본 응답
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

  private String generateUniqueLoginCode() {
    SecureRandom random = new SecureRandom();
    String code;
    do {
      code = "RC-" + (random.nextInt(9000) + 1000);
    } while (userRepository.findByLoginCode(code).isPresent());
    return code;
  }
}
