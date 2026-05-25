package com.kgu.life_watch.domain.health.service;

import com.kgu.life_watch.domain.health.entity.SleepStage;

public class SleepStageMapper {

  /** Apple Watch (iOS HealthKit) 수면 상태 문자열을 공통 SleepStage Enum으로 맵핑 */
  public static SleepStage fromAppleSleepValue(String value) {
    if (value == null) return SleepStage.WAKE;
    String valUpper = value.toUpperCase().trim();

    if (valUpper.contains("AWAKE") || valUpper.contains("INBED") || valUpper.contains("WAKE")) {
      return SleepStage.WAKE;
    } else if (valUpper.contains("REM")) {
      return SleepStage.REM;
    } else if (valUpper.contains("DEEP")) {
      return SleepStage.DEEP;
    } else {
      // Core, Asleep, Light, AsleepCore 등은 모두 LIGHT(얕은 수면)로 매핑
      return SleepStage.LIGHT;
    }
  }

  /** Galaxy Watch (Android Health Connect) 수면 상태 지표를 공통 SleepStage Enum으로 맵핑 */
  public static SleepStage fromGalaxySleepValue(Object value) {
    if (value == null) return SleepStage.WAKE;
    String valStr = value.toString().trim().toUpperCase();

    switch (valStr) {
      case "1":
      case "AWAKE":
      case "WAKE":
        return SleepStage.WAKE;
      case "2":
      case "REM":
        return SleepStage.REM;
      case "3":
      case "LIGHT":
      case "CORE":
      case "ASLEEP":
        return SleepStage.LIGHT;
      case "4":
      case "DEEP":
        return SleepStage.DEEP;
      default:
        return SleepStage.LIGHT;
    }
  }
}
