package com.kgu.life_watch.domain.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kgu.life_watch.domain.health.dto.request.WatchHealthDataRequest;
import com.kgu.life_watch.domain.health.dto.response.HealthDataResponse;
import com.kgu.life_watch.domain.health.service.HealthDataService;

@RestController
@RequestMapping("/api/health")
public class HealthDataController {

  private final HealthDataService healthDataService;

  public HealthDataController(HealthDataService healthDataService) {
    this.healthDataService = healthDataService;
  }

  @PostMapping("/sync")
  public ResponseEntity<HealthDataResponse> syncHealthData(
      @RequestBody WatchHealthDataRequest request) {
    healthDataService.syncHealthData(request);

    return ResponseEntity.ok(HealthDataResponse.success("health data sync success"));
  }
}
