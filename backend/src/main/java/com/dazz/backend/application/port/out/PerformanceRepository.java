package com.dazz.backend.application.port.out;

import com.dazz.backend.domain.performance.Performance;
import com.dazz.backend.domain.performance.PerformanceLineup;

import java.util.List;
import java.util.Optional;

public interface PerformanceRepository {

    Performance save(Performance performance);

    Optional<Performance> findById(Long id);

    List<Performance> findByClubId(Long clubId);

    PerformanceLineup saveLineup(PerformanceLineup lineup);

    List<PerformanceLineup> findLineupByPerformanceId(Long performanceId);

    List<PerformanceLineup> findLineupByMusicianId(Long musicianId);
}
