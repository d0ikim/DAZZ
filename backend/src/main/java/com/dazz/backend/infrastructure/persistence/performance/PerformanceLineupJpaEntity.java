package com.dazz.backend.infrastructure.persistence.performance;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "performance_lineup")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PerformanceLineupJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(name = "musician_id", nullable = false)
    private Long musicianId;

    @Column(name = "set_info", length = 100)
    private String setInfo;
}
