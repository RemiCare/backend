package com.kgu.life_watch.domain.notification.validator;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.user.entity.ElderlyProfile;
import com.kgu.life_watch.domain.user.entity.User;
import com.kgu.life_watch.domain.user.repository.ElderlyProfileRepository;
import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Component
@RequiredArgsConstructor
public class ElderlyAssignmentValidator {
  private final ElderlyProfileRepository elderlyProfileRepository;

  public ElderlyProfile validate(User worker, Long elderlyId) {
    ElderlyProfile elderly =
        elderlyProfileRepository
            .findByUserId(elderlyId)
            .orElseThrow(() -> LifelineException.from(ErrorCode.MEMBER_NOT_FOUND));
    if (!elderly.getSocialWorkerProfile().getUser().getId().equals(worker.getId())) {
      throw LifelineException.from(ErrorCode.INVALID_REQUEST);
    }
    return elderly;
  }
}
