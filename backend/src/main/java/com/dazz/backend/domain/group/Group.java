package com.dazz.backend.domain.group;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 뮤지션들이 속한 밴드/팀 그룹 (US-03 팀 정보).
 * genreTags: 장르 목록. DB에는 StringListConverter가 콤마 구분 문자열로 변환한다.
 */
@Getter
public class Group {

    private final Long id;
    private final String groupName;
    private final List<String> genreTags;
    private final String description;

    @Builder
    private Group(Long id, String groupName, List<String> genreTags, String description) {
        this.id = id;
        this.groupName = groupName;
        this.genreTags = genreTags;
        this.description = description;
    }
}
