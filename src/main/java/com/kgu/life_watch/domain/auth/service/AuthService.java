package com.kgu.life_watch.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.ElderlySignUpRequest;
import com.kgu.life_watch.domain.auth.dto.request.LoginRequest;
import com.kgu.life_watch.domain.auth.dto.request.SocialWorkerSignUpRequest;
import com.kgu.life_watch.domain.auth.dto.response.LoginResponse;
import com.kgu.life_watch.domain.chat.service.ChatRoomService;
import com.kgu.life_watch.domain.user.entity.ElderlyProfile;
import com.kgu.life_watch.domain.user.entity.SocialWorkerProfile;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.ElderlyProfileRepository;
import com.kgu.life_watch.domain.user.repository.SocialWorkerProfileRepository;
import com.kgu.life_watch.domain.user.repository.UserRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;
import com.kgu.life_watch.global.jwt.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final ElderlyProfileRepository elderlyProfileRepository;
  private final SocialWorkerProfileRepository socialWorkerProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthSmsService authSmsService;
  private final ChatRoomService chatRoomService;

  @Transactional
  public void signUpElderly(ElderlySignUpRequest request) {
    // if (!authSmsService.isVerified(request.phoneNumber())) {
    // throw LifelineException.from(ErrorCode.SMS_NOT_VERIFIED);
    // }

    if (userRepository.existsByLoginId(request.loginId())) {
      throw LifelineException.from(ErrorCode.ACCOUNT_USERNAME_EXIST);
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
            .role(User.Role.USER)
            .fcmToken(request.fcmToken())
            .build();

    ElderlyProfile.ElderlyProfileBuilder profileBuilder =
        ElderlyProfile.builder()
            .user(user)
            .drn(request.drn())
            .protectorContact(request.protectorContact())
            .protectorName(request.protectorName());

    // socialWorkerId가 존재하고 유효한 경우만 할당
    if (request.socialWorkerId() != null && !request.socialWorkerId().isBlank()) {
      Long socialWorkerId;
      try {
        socialWorkerId = Long.valueOf(request.socialWorkerId());
      } catch (NumberFormatException e) {
        throw LifelineException.from(ErrorCode.INVALID_REQUEST);
      }

      SocialWorkerProfile socialWorkerProfile =
          socialWorkerProfileRepository
              .findById(socialWorkerId)
              .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

      profileBuilder.socialWorkerProfile(socialWorkerProfile);
    }

    ElderlyProfile profile = profileBuilder.build();
    elderlyProfileRepository.save(profile);
    elderlyProfileRepository.flush();

    // 채팅방은 할당된 경우에만 생성
    if (profile.getSocialWorkerProfile() != null) {
      chatRoomService.createChatRoom(user, profile.getSocialWorkerProfile().getId());
    }
  }

  @Transactional
  public void signUpSocialWorker(SocialWorkerSignUpRequest request) {
    // if (!authSmsService.isVerified(request.phoneNumber())) {
    // throw LifelineException.from(ErrorCode.SMS_NOT_VERIFIED);
    // }
    if (userRepository.existsByLoginId(request.loginId())) {
      throw LifelineException.from(ErrorCode.ACCOUNT_USERNAME_EXIST);
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
            .role(User.Role.SOCIAL_WORKER)
            .fcmToken(request.fcmToken())
            .build();

    SocialWorkerProfile profile = SocialWorkerProfile.builder().user(user).build();
    socialWorkerProfileRepository.save(profile);
  }

  // string 대신 dto 반환해부리기
  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByLoginId(request.loginId())
            .orElseThrow(() -> LifelineException.from(ErrorCode.INCORRECT_ACCOUNT));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw LifelineException.from(ErrorCode.INCORRECT_PASSWORD);
    }

    String jwt = jwtTokenProvider.generateToken(user);

    if (user.getRole() == User.Role.USER && user.getElderlyProfile() != null) {
      ElderlyProfile elderly = user.getElderlyProfile();
      SocialWorkerProfile worker = elderly.getSocialWorkerProfile();

      String phoneNumber = user.getPhoneNumber();
      String address = user.getAddress();

      // NullPointerException 방지
      String socialWorkerName = null;
      String socialWorkerPhone = null;

      if (worker != null && worker.getUser() != null) {
        socialWorkerName = worker.getUser().getName();
        socialWorkerPhone = worker.getUser().getPhoneNumber();
      }

      return new LoginResponse(
          user.getName(),
          jwt,
          user.getBirthDate(),
          elderly.getProtectorName(),
          elderly.getProtectorContact(),
          socialWorkerName,
          socialWorkerPhone,
          user.getId(),
          false,
          phoneNumber,
          address);
    }

    if (user.getRole() == User.Role.SOCIAL_WORKER && user.getSocialWorkerProfile() != null) {
      return new LoginResponse(
          user.getName(),
          jwt,
          user.getBirthDate(),
          null,
          null,
          null,
          null,
          user.getId(),
          true,
          user.getPhoneNumber(),
          user.getAddress());
    }

    throw LifelineException.from(ErrorCode.INCORRECT_ACCOUNT);
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
  public void verifyIdentity(String loginId, String phoneNumber) {
    userRepository
        .findByLoginIdAndPhoneNumber(loginId, phoneNumber)
        .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
  }
}
