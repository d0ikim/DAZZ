package com.dazz.backend.domain.group;

import lombok.Builder;
import lombok.Getter;

/**
 * 뮤지션들이 속한 밴드/팀 그룹 (US-03 팀 정보).
 * genreTags: 콤마 구분 문자열로 저장 (e.g. "Jazz,Fusion,Bop").
 */
@Getter
public class Group {

    private final Long id;
    private final String groupName;
    private final String genreTags;
    private final String description;

    @Builder
    private Group(Long id, String groupName, String genreTags, String description) {
        this.id = id;
        this.groupName = groupName;
        this.genreTags = genreTags;
        this.description = description;
    }
}
