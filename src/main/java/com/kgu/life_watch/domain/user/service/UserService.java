package com.kgu.life_watch.domain.user.service;

import java.util.List;

import com.kgu.life_watch.domain.user.dto.request.WearableConnectionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.auth.dto.request.UserUpdateRequest;
import com.kgu.life_watch.domain.chat.entity.ChatRoom;
import com.kgu.life_watch.domain.chat.entity.RoomStatus;
import com.kgu.life_watch.domain.chat.entity.mapping.ChatParticipation;
import com.kgu.life_watch.domain.chat.repository.ChatRoomRepository;
import com.kgu.life_watch.domain.chat.service.ChatParticipationService;
import com.kgu.life_watch.domain.chat.service.ChatRoomService;
import com.kgu.life_watch.domain.user.dto.response.ElderlySimpleInfoResponse;
import com.kgu.life_watch.domain.user.dto.response.UserProfileResponse;
import com.kgu.life_watch.domain.user.entity.ElderlyProfile;
import com.kgu.life_watch.domain.user.entity.SocialWorkerProfile;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.ElderlyProfileRepository;
import com.kgu.life_watch.domain.user.repository.SocialWorkerProfileRepository;
import com.kgu.life_watch.domain.user.repository.UserRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@RequiredArgsConstructor
@Service
public class UserService {
  private final ElderlyProfileRepository elderlyProfileRepository;
  private final SocialWorkerProfileRepository socialWorkerProfileRepository;
  private final ChatRoomService chatRoomService;
  private final UserRepository userRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatParticipationService chatParticipationService;

  @Transactional
  public void assignElderly(Long elderlyId, Long socialWorkerId) {
    ElderlyProfile elderly =
        elderlyProfileRepository
            .findById(elderlyId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
    SocialWorkerProfile socialWorker =
        socialWorkerProfileRepository
            .findById(socialWorkerId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    // 연관관계 편의 메서드를 통해 노인을 사회복지사에게 할당
    socialWorker.addElderly(elderly);

    // 기존 채팅방 존재 여부 확인
    List<ChatParticipation> participationList =
        chatParticipationService.getParticipationByUser(socialWorker.getUser());

    boolean chatRoomExists = false;

    for (ChatParticipation participation : participationList) {
      ChatRoom chatRoom = participation.getChatRoom();

      List<Long> participantIds =
          chatRoom.getParticipation().stream().map(p -> p.getUser().getId()).toList();

      if (participantIds.size() == 2
          && participantIds.contains(socialWorker.getUser().getId())
          && participantIds.contains(elderly.getUser().getId())) {
        // 기존 채팅방 존재 → 상태만 ACTIVE로 변경
        chatRoom.updateStatus(RoomStatus.ACTIVATE);
        chatRoomExists = true;
        break;
      }
    }

    // 없으면 새로 생성
    if (!chatRoomExists) {
      chatRoomService.createChatRoom(elderly.getUser(), socialWorkerId);
    }
  }

  @Transactional
  public void unassignElderly(Long elderlyId, Long socialWorkerId) {
    ElderlyProfile elderly =
        elderlyProfileRepository
            .findById(elderlyId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
    SocialWorkerProfile socialWorker =
        socialWorkerProfileRepository
            .findById(socialWorkerId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    socialWorker.getAssignedSeniors().remove(elderly);
    // 노인의 사회복지사 연관관계 해제
    elderly.setSocialWorkerProfile(null);

    // 채팅방 상태 바꾸기
    // 1. 참여 중인 채팅방 리스트 가져오기 (사회복지사 기준)
    List<ChatParticipation> participationList =
        chatParticipationService.getParticipationByUser(socialWorker.getUser());

    // 2. 노인과 둘 다 참여한 채팅방 찾기
    for (ChatParticipation participation : participationList) {
      ChatRoom chatRoom = participation.getChatRoom();

      List<Long> participantIds =
          chatRoom.getParticipation().stream().map(p -> p.getUser().getId()).toList();

      if (participantIds.size() == 2
          && participantIds.contains(socialWorker.getUser().getId())
          && participantIds.contains(elderly.getUser().getId())) {

        chatRoom.updateStatus(RoomStatus.DEACTIVATE);
        break;
      }
    }
  }

  @Transactional(readOnly = true)
  public UserProfileResponse getProfile(User user) {
    return UserProfileResponse.toDto(user);
  }

  @Transactional
  public void updateUserInfo(Long userId, UserUpdateRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    user.updateBasicInfo(request.name(), request.phoneNumber(), request.address());

    if (user.getRole() == User.Role.USER && user.getElderlyProfile() != null) {
      ElderlyProfile profile = user.getElderlyProfile();
      profile.updateProtector(request.protectorName(), request.protectorContact());
    }
  }

  @Transactional(readOnly = true)
  public List<ElderlySimpleInfoResponse> getAssignableElderlyList() {
    return elderlyProfileRepository.findAllBySocialWorkerProfileIsNull().stream()
        .map(profile -> ElderlySimpleInfoResponse.from(profile.getUser()))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ElderlySimpleInfoResponse> getAssignedElderlyList(User socialWorkerUser) {
    Long socialWorkerProfileId = socialWorkerUser.getSocialWorkerProfile().getId();
    return elderlyProfileRepository.findAllBySocialWorkerProfileId(socialWorkerProfileId).stream()
        .map(profile -> ElderlySimpleInfoResponse.from(profile.getUser()))
        .toList();
  }

  @Transactional
  public void updateWearableConnectionStatus(Long userId, WearableConnectionRequest request) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

    // 노인 프로필 가져와서 상태 업데이트
    ElderlyProfile profile = user.getElderlyProfile();
    if (profile != null) {
      profile.updateWearableConnection(request.isConnected(), request.deviceName());
    } else {
      throw new IllegalArgumentException("노인 프로필이 존재하지 않습니다.");
    }
  }
}
