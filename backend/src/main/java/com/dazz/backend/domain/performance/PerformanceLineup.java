package com.dazz.backend.domain.performance;

import lombok.Builder;
import lombok.Getter;

/**
 * 공연-뮤지션 N:M 관계.
 * setInfo: "1부", "2부" 등 세트 상세 정보.
 * musicianId NOT NULL — 라인업 클릭 시 반드시 뮤지션 프로필로 연결 보장.
 */
@Getter
public class PerformanceLineup {

    private final Long id;
    private final Long performanceId;
    private final Long musicianId;
    private final String setInfo;

    @Builder
    private PerformanceLineup(Long id, Long performanceId, Long musicianId, String setInfo) {
        this.id = id;
        this.performanceId = performanceId;
        this.musicianId = musicianId;
        this.setInfo = setInfo;
    }
}
