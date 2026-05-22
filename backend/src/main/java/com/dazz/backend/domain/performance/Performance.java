package com.dazz.backend.domain.performance;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공연 Aggregate Root.
 * setList: 당일 연주 곡 리스트 정보 (자유 텍스트).
 */
@Getter
public class Performance {

    private final Long id;
    private final Long clubId;
    private final LocalDateTime startTime;
    private final String title;
    private final String genre;
    private final String setList;

    @Builder
    private Performance(Long id, Long clubId, LocalDateTime startTime,
                        String title, String genre, String setList) {
        this.id = id;
        this.clubId = clubId;
        this.startTime = startTime;
        this.title = title;
        this.genre = genre;
        this.setList = setList;
    }
}
