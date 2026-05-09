package com.kgu.life_watch.domain.auth.service;

import java.security.SecureRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.*;
import com.kgu.life_watch.domain.auth.dto.response.*;
import com.kgu.life_watch.domain.chat.service.ChatRoomService;
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
  private final ChatRoomService chatRoomService;

  @Transactional
  public void signUpElderly(ElderlySignUpRequest request) {
    if (userRepository.existsByLoginId(request.loginId())) {
      throw LifelineException.from(ErrorCode.ACCOUNT_USERNAME_EXIST);
    }
    if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
      throw LifelineException.from(ErrorCode.DUPLICATE_PHONE_NUMBER);
    }

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
            .role(User.Role.ELDER) // 노인으로 가입 시 ELDER 역할 부여
            .fcmToken(request.fcmToken())
            .loginCode(generateUniqueLoginCode()) // RC-XXXX 코드 생성
            .build();

    ElderlyProfile.ElderlyProfileBuilder profileBuilder =
        ElderlyProfile.builder()
            .user(user)
            .drn(request.drn())
            .protectorContact(request.protectorContact())
            .protectorName(request.protectorName());

    if (request.protectorId() != null && !request.protectorId().isBlank()) {
      Long protectorId;
      try {
        protectorId = Long.valueOf(request.protectorId());
      } catch (NumberFormatException e) {
        throw LifelineException.from(ErrorCode.INVALID_REQUEST);
      }

      ProtectorProfile protectorProfile =
          protectorProfileRepository
              .findById(protectorId)
              .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

      profileBuilder.protectorProfile(protectorProfile);
    }

    ElderlyProfile profile = profileBuilder.build();
    elderlyProfileRepository.save(profile);
    elderlyProfileRepository.flush();

    if (profile.getProtectorProfile() != null) {
      chatRoomService.createChatRoom(user, profile.getProtectorProfile().getId());
    }
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

  @Transactional
  public void signUpProtector(ProtectorSignUpRequest request) {
    if (userRepository.existsByLoginId(request.loginId())) {
      throw LifelineException.from(ErrorCode.ACCOUNT_USERNAME_EXIST);
    }
    if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
      throw LifelineException.from(ErrorCode.DUPLICATE_PHONE_NUMBER);
    }

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

  @Transactional(readOnly = true)
  public void verifyIdentity(String loginId, String phoneNumber) {
    userRepository
        .findByLoginIdAndPhoneNumber(loginId, phoneNumber)
        .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
  }
}
