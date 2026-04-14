package com.kgu.life_watch.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.FindIdRequest;
import com.kgu.life_watch.domain.auth.dto.request.FindPasswordRequest;
import com.kgu.life_watch.domain.auth.dto.request.PasswordChangeRequest;
import com.kgu.life_watch.domain.auth.dto.request.ResetPasswordRequest;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.UserRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Service
@RequiredArgsConstructor
public class PasswordService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthSmsService authSmsService;

  @Transactional
  public void changePassword(PasswordChangeRequest request, User userFromPrincipal) {
    User user =
        userRepository
            .findById(userFromPrincipal.getId())
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw LifelineException.from(ErrorCode.INCORRECT_PASSWORD);
    }

    user.changePassword(passwordEncoder.encode(request.newPassword()));
  }

  @Transactional(readOnly = true)
  public String findLoginId(FindIdRequest request) {
    return userRepository
        .findByNameAndPhoneNumber(request.name(), request.phoneNumber())
        .map(User::getLoginId)
        .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
  }

  @Transactional
  public void sendPasswordResetCode(FindPasswordRequest request) {
    userRepository
        .findByLoginIdAndPhoneNumber(request.loginId(), request.phoneNumber())
        .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    authSmsService.sendAuthenticationCode(request.phoneNumber());
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    User user =
        userRepository
            .findByLoginId(request.loginId())
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    if (!authSmsService.verifyCode(user.getPhoneNumber(), request.verificationCode())) {
      throw LifelineException.from(ErrorCode.SMS_VERIFICATION_FAILED);
    }

    user.changePassword(passwordEncoder.encode(request.newPassword()));
  }
}
