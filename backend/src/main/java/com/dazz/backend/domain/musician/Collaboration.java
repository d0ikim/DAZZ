package com.dazz.backend.domain.musician;

import lombok.Builder;
import lombok.Getter;

/**
 * 뮤지션 간 협업 관계.
 * - weight: 협업 횟수(친밀도). 동시 업데이트 시 분산락 대상.
 * - from < to 정규화는 DB 제약 또는 서비스 레이어에서 보장한다.
 */
@Getter
public class Collaboration {

    private final Long id;
    private final Long fromMusicianId;
    private final Long toMusicianId;
    private final RelationType relationType;
    private final int weight;

    @Builder
    private Collaboration(Long id, Long fromMusicianId, Long toMusicianId,
                          RelationType relationType, int weight) {
        this.id = id;
        this.fromMusicianId = fromMusicianId;
        this.toMusicianId = toMusicianId;
        this.relationType = relationType;
        this.weight = weight;
    }

    public Collaboration incrementWeight() {
        return new Collaboration(this.id, this.fromMusicianId, this.toMusicianId,
                this.relationType, this.weight + 1);
    }
}
