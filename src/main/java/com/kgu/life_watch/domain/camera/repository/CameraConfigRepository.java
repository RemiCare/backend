package com.kgu.life_watch.domain.camera.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kgu.life_watch.domain.camera.entity.CameraConfig;

public interface CameraConfigRepository extends JpaRepository<CameraConfig, Long> {}
