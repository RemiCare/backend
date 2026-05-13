package com.kgu.life_watch.domain.camera.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CameraConfig {

  @Id private Long elderlyId; // 어르신(User)의 ID를 식별자로 사용

  // B구역 (낙상 감지 구역 - 거실 등) - 비율값 (0.0 ~ 1.0)
  @Column(name = "b_x", nullable = false)
  private Double bX;

  @Column(name = "b_y", nullable = false)
  private Double bY;

  @Column(name = "b_w", nullable = false)
  private Double bW;

  @Column(name = "b_h", nullable = false)
  private Double bH;

  // C구역 (위험 접근 구역 - 화장실 문 등) - 비율값 (0.0 ~ 1.0)
  @Column(name = "c_x", nullable = false)
  private Double cX;

  @Column(name = "c_y", nullable = false)
  private Double cY;

  @Column(name = "c_w", nullable = false)
  private Double cW;

  @Column(name = "c_h", nullable = false)
  private Double cH;

  public void updateZones(
      Double bX, Double bY, Double bW, Double bH, Double cX, Double cY, Double cW, Double cH) {
    this.bX = bX;
    this.bY = bY;
    this.bW = bW;
    this.bH = bH;
    this.cX = cX;
    this.cY = cY;
    this.cW = cW;
    this.cH = cH;
  }
}
