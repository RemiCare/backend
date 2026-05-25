package com.kgu.life_watch.domain.camera.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.domain.camera.dto.request.CameraZoneRequest;
import com.kgu.life_watch.domain.camera.dto.response.CameraZoneResponse;
import com.kgu.life_watch.domain.camera.entity.CameraConfig;
import com.kgu.life_watch.domain.camera.repository.CameraConfigRepository;

@Service
@RequiredArgsConstructor
public class CameraConfigService {

  private final CameraConfigRepository cameraConfigRepository;

  /** [구역 설정 저장/업데이트] 기존 설정이 있으면 업데이트, 없으면 신규 생성 */
  @Transactional
  public void saveOrUpdateZones(CameraZoneRequest request) {
    cameraConfigRepository
        .findById(request.elderlyId())
        .ifPresentOrElse(
            config ->
                config.updateZones(
                    request.bX(),
                    request.bY(),
                    request.bW(),
                    request.bH(),
                    request.cX(),
                    request.cY(),
                    request.cW(),
                    request.cH()),
            () ->
                cameraConfigRepository.save(
                    CameraConfig.builder()
                        .elderlyId(request.elderlyId())
                        .bX(request.bX())
                        .bY(request.bY())
                        .bW(request.bW())
                        .bH(request.bH())
                        .cX(request.cX())
                        .cY(request.cY())
                        .cW(request.cW())
                        .cH(request.cH())
                        .build()));
  }

  /** [구역 설정 조회] 특정 어르신의 카메라 구역 설정값 조회 */
  @Transactional(readOnly = true)
  public CameraZoneResponse getZones(Long elderlyId) {
    return cameraConfigRepository.findById(elderlyId).map(CameraZoneResponse::from).orElse(null);
  }
}
