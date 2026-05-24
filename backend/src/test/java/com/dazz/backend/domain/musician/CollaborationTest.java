package com.dazz.backend.domain.musician;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Collaboration 도메인 단위 테스트")
class CollaborationTest {

    @Test
    @DisplayName("incrementWeight() 호출 시 weight가 1 증가한 새 객체를 반환한다")
    void incrementWeight_성공시_weight가_1_증가한_새_객체를_반환한다() {
        // Given
        Collaboration collab = Collaboration.builder()
                .id(1L)
                .fromMusicianId(10L)
                .toMusicianId(20L)
                .relationType(RelationType.COLLABORATION)
                .weight(5)
                .build();

        // When
        Collaboration updated = collab.incrementWeight();

        // Then
        assertThat(updated.getWeight()).isEqualTo(6);
    }

    @Test
    @DisplayName("incrementWeight() 호출 후 원본 객체의 weight는 변하지 않는다")
    void incrementWeight_원본_객체는_변하지_않는다() {
        // Given
        Collaboration collab = Collaboration.builder()
                .id(1L)
                .fromMusicianId(10L)
                .toMusicianId(20L)
                .relationType(RelationType.COLLABORATION)
                .weight(5)
                .build();

        // When
        collab.incrementWeight();

        // Then — 불변 객체이므로 원본은 그대로
        assertThat(collab.getWeight()).isEqualTo(5);
    }
}
