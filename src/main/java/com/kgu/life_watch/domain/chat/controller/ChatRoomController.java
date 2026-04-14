package com.kgu.life_watch.domain.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.chat.dto.response.ChatMessageResponse;
import com.kgu.life_watch.domain.chat.dto.response.ChatRoomResponse;
import com.kgu.life_watch.domain.chat.service.ChatMessageService;
import com.kgu.life_watch.domain.chat.service.ChatRoomService;
import com.kgu.life_watch.global.domain.SuccessCode;
import com.kgu.life_watch.global.dto.response.ApiResponse;
import com.kgu.life_watch.global.security.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-room")
@Tag(name = "ChatRoomController", description = "채팅방 관련 API")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;
  private final ChatMessageService chatMessageService;

  @GetMapping("/create")
  @Operation(summary = "채팅방 생성 API", description = "채팅방을 생성하는 API입니다.")
  public ApiResponse<ChatRoomResponse> createChatRoom(
      @AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam Long receiverId) {
    return new ApiResponse<>(chatRoomService.createChatRoom(userDetails.user(), receiverId));
  }

  @GetMapping("/list")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "참여중인 채팅방 조회 API", description = "본인이 참여중인 채팅방을 조회하는 API입니다.")
  public ApiResponse<ChatRoomResponse> getMessagesByRoom(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return new ApiResponse<>(chatRoomService.getAllRoom(userDetails.user()));
  }

  @DeleteMapping("/delete/{roomId}")
  @Operation(summary = "채팅방 나가기(삭제) API", description = "채팅방을 나가기(삭제)하는 API입니다.")
  public ApiResponse<Void> deleteRoom(
      @PathVariable("roomId") Long roomId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    chatRoomService.deleteRoom(roomId, userDetails.user());
    return new ApiResponse<>(SuccessCode.REQUEST_OK);
  }

  @GetMapping("/messages/{roomId}")
  @Operation(summary = "채팅방 전체 메시지 조회 API", description = "특정 채팅방의 모든 메시지를 조회하는 API입니다.")
  public ApiResponse<ChatMessageResponse> getAllMessagesByRoom(
      @PathVariable("roomId") Long roomId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    return new ApiResponse<>(chatMessageService.getMessagesByRoom(roomId, userDetails.user()));
  }
}
