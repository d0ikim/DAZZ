package com.dazz.backend.domain.group;

import lombok.Builder;
import lombok.Getter;

/**
 * 그룹-뮤지션 N:M 관계. role은 팀 내 역할 (e.g. "리더", "베이시스트").
 */
@Getter
public class GroupMember {

    private final Long id;
    private final Long groupId;
    private final Long musicianId;
    private final String role;

    @Builder
    private GroupMember(Long id, Long groupId, Long musicianId, String role) {
        this.id = id;
        this.groupId = groupId;
        this.musicianId = musicianId;
        this.role = role;
    }
}
