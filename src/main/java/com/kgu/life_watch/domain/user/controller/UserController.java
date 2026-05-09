package com.kgu.life_watch.domain.user.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.UserUpdateRequest;
import com.kgu.life_watch.domain.user.dto.request.ElderlyRegisterRequest;
import com.kgu.life_watch.domain.user.dto.request.WearableConnectionRequest;
import com.kgu.life_watch.domain.user.dto.response.ElderlySimpleInfoResponse;
import com.kgu.life_watch.domain.user.dto.response.UserProfileResponse;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.service.UserService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;
import com.kgu.life_watch.global.security.CustomUserDetails;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
@Tag(name = "UserController", description = "유저 관련 API")
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  @Operation(summary = "내 정보 조회 API", description = "내 정보를 조회하는 API입니다.")
  public ApiResponse<UserProfileResponse> getCurrentUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    User user = userDetails.user();
    return new ApiResponse<>(userService.getProfile(user));
  }

  @GetMapping("/role-check")
  @Operation(summary = "내 역할 조회 API", description = "내 역할을 조회하는 API입니다.")
  public ApiResponse<String> checkRole(@AuthenticationPrincipal CustomUserDetails userDetails) {
    User user = userDetails.user();

    if (user.getRole() == User.Role.PROTECTOR) {
      return new ApiResponse<>("보호자입니다.");
    } else if (user.getRole() == User.Role.ELDER) {
      return new ApiResponse<>("어르신 사용자입니다.");
    }
    return new ApiResponse<>("알 수 없는 역할");
  }

  @PostMapping("/elderly")
  @Operation(summary = "어르신 추가 등록 API", description = "보호자가 관리할 어르신을 시스템에 추가로 등록합니다.")
  public ApiResponse<Void> registerAdditionalElderly(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid ElderlyRegisterRequest request) {
    userService.registerElderly(userDetails.user(), request);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @DeleteMapping("/elderly/{elderlyId}")
  @Operation(summary = "어르신 등록 해제 API", description = "관리 중인 어르신과의 연결을 해제합니다.")
  public ApiResponse<Void> removeElderly(
      @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long elderlyId) {
    userService.removeElderly(userDetails.user(), elderlyId);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @GetMapping("/elderly")
  @Operation(summary = "관리 중인 어르신 목록 조회 API", description = "내가 등록하여 관리 중인 어르신들의 목록을 조회합니다.")
  // 🌟 반환 타입을 ApiResponse<ElderlySimpleInfoResponse>로 수정 (List 제거)
  public ApiResponse<ElderlySimpleInfoResponse> getMyElderlyList(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    List<ElderlySimpleInfoResponse> elderlyList = userService.getMyElderlyList(userDetails.user());

    // 🌟 정적 메서드를 사용하면 타입 추론 에러 없이 아주 깔끔하게 반환됩니다.
    return ApiResponse.ok(elderlyList);
  }

  @PatchMapping("/me")
  @Operation(summary = "회원정보 수정 API", description = "이름, 주소, 전화번호, (노인의 경우 보호자 정보)를 수정합니다.")
  public ApiResponse<Void> updateUserInfo(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid UserUpdateRequest request) {
    userService.updateUserInfo(userDetails.user().getId(), request);
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @PatchMapping("/profile/wearable-connection")
  @Operation(summary = "웨어러블 기기 연결 상태 업데이트", description = "스마트워치 등의 웨어러블 기기 연결 상태를 수동으로 변경합니다.")
  public ApiResponse<Void> updateWearableConnection(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody @Valid WearableConnectionRequest request) {

    userService.updateWearableConnectionStatus(userDetails.user().getId(), request);

    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }
}
