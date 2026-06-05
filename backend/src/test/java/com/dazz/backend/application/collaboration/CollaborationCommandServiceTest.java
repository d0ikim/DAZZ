package com.dazz.backend.application.collaboration;

import com.dazz.backend.application.collaboration.command.CollaborationLinkCommand;
import com.dazz.backend.application.port.out.CollaborationRepository;
import com.dazz.backend.application.port.out.MusicianRepository;
import com.dazz.backend.domain.musician.Collaboration;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.Position;
import com.dazz.backend.domain.musician.RelationType;
import com.dazz.backend.domain.musician.exception.CollaborationSelfReferenceException;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollaborationCommandService 단위 테스트")
class CollaborationCommandServiceTest {

    @Mock
    private CollaborationRepository collaborationRepository;

    @Mock
    private MusicianRepository musicianRepository;

    @InjectMocks
    private CollaborationCommandService commandService;

    // ── 신규 협업 관계 생성 ──────────────────────────────────────────────────

    @Test
    @DisplayName("linkOrIncrement() - 성공: 신규 협업 관계 생성, weight=1, created=true")
    void linkOrIncrement_success_newPair() {
        // Given
        CollaborationLinkCommand command =
                new CollaborationLinkCommand(102L, 205L, RelationType.COLLABORATION);

        stubMusiciansExist(102L, 205L);
        when(collaborationRepository.findByFromAndToAndType(102L, 205L, RelationType.COLLABORATION))
                .thenReturn(Optional.empty());

        Collaboration saved = Collaboration.newPair(102L, 205L, RelationType.COLLABORATION);
        saved = Collaboration.builder().id(1L).fromMusicianId(102L).toMusicianId(205L)
                .relationType(RelationType.COLLABORATION).weight(1).build();
        when(collaborationRepository.save(any())).thenReturn(saved);

        // When
        CollaborationResult result = commandService.linkOrIncrement(command);

        // Then
        assertThat(result.weight()).isEqualTo(1);
        assertThat(result.created()).isTrue();
        verify(collaborationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("linkOrIncrement() - 성공: 기존 협업 관계 weight 증가, created=false")
    void linkOrIncrement_success_incrementWeight() {
        // Given
        CollaborationLinkCommand command =
                new CollaborationLinkCommand(102L, 205L, RelationType.COLLABORATION);

        stubMusiciansExist(102L, 205L);

        Collaboration existing = Collaboration.builder().id(1L).fromMusicianId(102L).toMusicianId(205L)
                .relationType(RelationType.COLLABORATION).weight(3).build();
        Collaboration updated = Collaboration.builder().id(1L).fromMusicianId(102L).toMusicianId(205L)
                .relationType(RelationType.COLLABORATION).weight(4).build();

        when(collaborationRepository.findByFromAndToAndType(102L, 205L, RelationType.COLLABORATION))
                .thenReturn(Optional.of(existing));
        when(collaborationRepository.save(any())).thenReturn(updated);

        // When
        CollaborationResult result = commandService.linkOrIncrement(command);

        // Then
        assertThat(result.weight()).isEqualTo(4);
        assertThat(result.created()).isFalse();
    }

    @Test
    @DisplayName("linkOrIncrement() - 성공: fromId > toId 순서로 입력해도 min:max 정규화 후 조회")
    void linkOrIncrement_normalizes_ids_minMax() {
        // Given — 의도적으로 큰 ID를 from에 넣음
        CollaborationLinkCommand command =
                new CollaborationLinkCommand(205L, 102L, RelationType.COLLABORATION);

        stubMusiciansExist(205L, 102L);
        when(collaborationRepository.findByFromAndToAndType(102L, 205L, RelationType.COLLABORATION))
                .thenReturn(Optional.empty());

        Collaboration saved = Collaboration.builder().id(1L).fromMusicianId(102L).toMusicianId(205L)
                .relationType(RelationType.COLLABORATION).weight(1).build();
        when(collaborationRepository.save(any())).thenReturn(saved);

        // When
        commandService.linkOrIncrement(command);

        // Then: 정규화된 (102, 205)로 조회됐는지 검증
        verify(collaborationRepository).findByFromAndToAndType(102L, 205L, RelationType.COLLABORATION);
    }

    // ── Unhappy Path ────────────────────────────────────────────────────────

    @Test
    @DisplayName("linkOrIncrement() - 실패: fromId == toId 이면 CollaborationSelfReferenceException")
    void linkOrIncrement_throws_selfReference() {
        // Given
        CollaborationLinkCommand command =
                new CollaborationLinkCommand(102L, 102L, RelationType.COLLABORATION);

        // When & Then
        assertThatThrownBy(() -> commandService.linkOrIncrement(command))
                .isInstanceOf(CollaborationSelfReferenceException.class);

        verifyNoInteractions(collaborationRepository);
    }

    @Test
    @DisplayName("linkOrIncrement() - 실패: fromMusicianId에 해당하는 뮤지션 없으면 MusicianNotFoundException")
    void linkOrIncrement_throws_musicianNotFound_fromId() {
        // Given
        CollaborationLinkCommand command =
                new CollaborationLinkCommand(999L, 205L, RelationType.COLLABORATION);

        when(musicianRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commandService.linkOrIncrement(command))
                .isInstanceOf(MusicianNotFoundException.class);
    }

    @Test
    @DisplayName("linkOrIncrement() - 실패: toMusicianId에 해당하는 뮤지션 없으면 MusicianNotFoundException")
    void linkOrIncrement_throws_musicianNotFound_toId() {
        // Given
        CollaborationLinkCommand command =
                new CollaborationLinkCommand(102L, 999L, RelationType.COLLABORATION);

        when(musicianRepository.findById(102L)).thenReturn(Optional.of(
                Musician.builder().id(102L).stageName("김재즈").position(Position.PIANO).build()));
        when(musicianRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commandService.linkOrIncrement(command))
                .isInstanceOf(MusicianNotFoundException.class);
    }

    // ── 헬퍼 ────────────────────────────────────────────────────────────────

    private void stubMusiciansExist(Long id1, Long id2) {
        Musician m1 = Musician.builder().id(id1).stageName("A").position(Position.PIANO).build();
        Musician m2 = Musician.builder().id(id2).stageName("B").position(Position.BASS).build();
        when(musicianRepository.findById(id1)).thenReturn(Optional.of(m1));
        when(musicianRepository.findById(id2)).thenReturn(Optional.of(m2));
    }
}