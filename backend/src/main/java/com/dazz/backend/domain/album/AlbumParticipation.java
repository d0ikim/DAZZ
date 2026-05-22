package com.dazz.backend.domain.album;

import lombok.Builder;
import lombok.Getter;

/**
 * 앨범-뮤지션 N:M 관계를 담는 참여 엔티티.
 * 새 역할(e.g. 빅밴드 컨덕터) 추가 시 ParticipationType 값만 추가하면 스키마 변경이 없다.
 */
@Getter
public class AlbumParticipation {

    private final Long id;
    private final Long albumId;
    private final Long musicianId;
    private final ParticipationType participationType;

    @Builder
    private AlbumParticipation(Long id, Long albumId, Long musicianId,
                               ParticipationType participationType) {
        this.id = id;
        this.albumId = albumId;
        this.musicianId = musicianId;
        this.participationType = participationType;
    }
}
