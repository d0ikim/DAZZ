package com.dazz.backend.infrastructure.persistence.performance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceJpaRepository extends JpaRepository<PerformanceJpaEntity, Long> {

    List<PerformanceJpaEntity> findByClubId(Long clubId);
}
