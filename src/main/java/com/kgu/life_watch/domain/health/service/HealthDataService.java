package com.kgu.life_watch.domain.health.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.health.dto.request.GalaxyWatchHealthDataRequest;
import com.kgu.life_watch.domain.health.dto.response.HealthDataResponse;
import com.kgu.life_watch.domain.health.entity.HealthData;
import com.kgu.life_watch.domain.health.repository.HealthDataRepository;
import com.kgu.life_watch.domain.notification.validator.ElderlyAssignmentValidator;
import com.kgu.life_watch.domain.user.entity.ElderlyProfile;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.UserRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Service
@RequiredArgsConstructor
public class HealthDataService {

  private final HealthDataRepository healthDataRepository;
  private final UserRepository userRepository;
  private final ElderlyAssignmentValidator elderlyAssignmentValidator;
  private final ObjectMapper objectMapper;

  @Transactional
  public HealthDataResponse saveGalaxyWatchData(Long userId, GalaxyWatchHealthDataRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));

    if (user.getRole() != User.Role.USER) {
      throw LifelineException.from(ErrorCode.NOT_ENOUGH_PERMISSION);
    }

    ElderlyProfile elderlyProfile = user.getElderlyProfile();

    if (elderlyProfile == null) {
      throw LifelineException.from(ErrorCode.MEMBER_NOT_FOUND);
    }

    if (request.deviceName() != null && !request.deviceName().isBlank()) {
      elderlyProfile.updateWearableConnection(true, request.deviceName());
    }

    HealthData healthData =
        HealthData.builder()
            .user(user)
            .measuredAt(request.measuredAt())
            .deviceName(request.deviceName())
            .heartRate(request.heartRate())
            .stepCount(request.stepCount())
            .bloodOxygen(request.bloodOxygen())
            .totalSleepMinutes(request.totalSleepMinutes())
            .awakeMinutes(request.awakeMinutes())
            .rawData(convertRawDataToString(request))
            .build();

    HealthData savedHealthData = healthDataRepository.save(healthData);

    return HealthDataResponse.from(savedHealthData);
  }

  @Transactional(readOnly = true)
  public HealthDataResponse getMyLatestHealthData(Long userId) {
    return healthDataRepository
        .findTopByUserIdOrderByMeasuredAtDesc(userId)
        .map(HealthDataResponse::from)
        .orElseThrow(() -> LifelineException.from(ErrorCode.HEALTH_DATA_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public List<HealthDataResponse> getMyHealthDataList(
      Long userId,
      LocalDateTime from,
      LocalDateTime to
  ) {
    List<HealthData> healthDataList;

    if (from != null && to != null) {
      healthDataList =
          healthDataRepository.findAllByUserIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(
              userId,
              from,
              to
          );
    } else if (from != null) {
      healthDataList =
          healthDataRepository.findAllByUserIdAndMeasuredAtAfterOrderByMeasuredAtDesc(
              userId,
              from
          );
    } else if (to != null) {
      healthDataList =
          healthDataRepository.findAllByUserIdAndMeasuredAtBeforeOrderByMeasuredAtDesc(
              userId,
              to
          );
    } else {
      healthDataList = healthDataRepository.findAllByUserIdOrderByMeasuredAtDesc(userId);
    }

    return healthDataList.stream()
        .map(HealthDataResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public HealthDataResponse getElderlyLatestHealthData(User socialWorker, Long elderlyId) {
    ElderlyProfile elderlyProfile = elderlyAssignmentValidator.validate(socialWorker, elderlyId);

    return healthDataRepository
        .findTopByUserIdOrderByMeasuredAtDesc(elderlyProfile.getUser().getId())
        .map(HealthDataResponse::from)
        .orElseThrow(() -> LifelineException.from(ErrorCode.HEALTH_DATA_NOT_FOUND));
  }

  private String convertRawDataToString(GalaxyWatchHealthDataRequest request) {
    if (request.rawData() == null) {
      return null;
    }

    try {
      return objectMapper.writeValueAsString(request.rawData());
    } catch (JsonProcessingException e) {
      throw LifelineException.from(ErrorCode.INVALID_REQUEST);
    }
  }
}