package com.dazz.backend.domain.performance;

import lombok.Builder;
import lombok.Getter;

/**
 * 공연이 열리는 재즈 클럽/공연장. ERD의 CLUB 테이블에 대응.
 */
@Getter
public class Club {

    private final Long id;
    private final String name;
    private final String location;
    private final String instagramUrl;

    @Builder
    private Club(Long id, String name, String location, String instagramUrl) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.instagramUrl = instagramUrl;
    }
}
