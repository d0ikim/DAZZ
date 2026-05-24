package com.dazz.backend.infrastructure.persistence.performance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceLineupJpaRepository extends JpaRepository<PerformanceLineupJpaEntity, Long> {

    List<PerformanceLineupJpaEntity> findByPerformanceId(Long performanceId);

    List<PerformanceLineupJpaEntity> findByMusicianId(Long musicianId);
}
