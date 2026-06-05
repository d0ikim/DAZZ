package com.dazz.backend.application.musician;

import com.dazz.backend.application.musician.command.MusicianRegisterCommand;
import com.dazz.backend.application.port.out.MusicianRepository;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.Position;
import com.dazz.backend.domain.musician.VerificationTier;
import com.dazz.backend.domain.musician.exception.MusicianAlreadyClaimedException;
import com.dazz.backend.domain.musician.exception.MusicianInvalidException;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import com.dazz.backend.domain.musician.exception.MusicianUserAlreadyLinkedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MusicianCommandService 단위 테스트")
class MusicianCommandServiceTest {

    @Mock
    private MusicianRepository musicianRepository;

    @InjectMocks
    private MusicianCommandService commandService;

    // ── register() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("register() - 성공: 유효한 command로 뮤지션이 저장된다")
    void register_success() {
        // Given
        MusicianRegisterCommand command = new MusicianRegisterCommand(
                "김재즈", null, Position.PIANO, null, null, null);
        Musician saved = Musician.builder()
                .id(1L).stageName("김재즈").position(Position.PIANO).build();
        when(musicianRepository.save(any())).thenReturn(saved);

        // When
        Musician result = commandService.register(command);

        // Then
        assertThat(result.getStageName()).isEqualTo("김재즈");
        verify(musicianRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("register() - 실패: stageName이 null이면 MusicianInvalidException 발생 (저장 미호출)")
    void register_throwsException_whenStageNameIsNull() {
        // Given
        MusicianRegisterCommand command = new MusicianRegisterCommand(
                null, null, Position.PIANO, null, null, null);

        // When & Then
        assertThatThrownBy(() -> commandService.register(command))
                .isInstanceOf(MusicianInvalidException.class);
        verify(musicianRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() - 실패: stageName이 공백이면 MusicianInvalidException 발생")
    void register_throwsException_whenStageNameIsBlank() {
        // Given
        MusicianRegisterCommand command = new MusicianRegisterCommand(
                "  ", null, Position.PIANO, null, null, null);

        // When & Then
        assertThatThrownBy(() -> commandService.register(command))
                .isInstanceOf(MusicianInvalidException.class);
        verify(musicianRepository, never()).save(any());
    }

    @Test
    @DisplayName("register() - 실패: position이 null이면 MusicianInvalidException 발생")
    void register_throwsException_whenPositionIsNull() {
        // Given
        MusicianRegisterCommand command = new MusicianRegisterCommand(
                "김재즈", null, null, null, null, null);

        // When & Then
        assertThatThrownBy(() -> commandService.register(command))
                .isInstanceOf(MusicianInvalidException.class);
        verify(musicianRepository, never()).save(any());
    }

    // ── claim() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("claim() - 성공: unclaimed 뮤지션에 userId가 연결되고 UNVERIFIED 등급이 된다")
    void claim_success() {
        // Given
        UUID uuid = UUID.randomUUID();
        Long userId = 42L;
        Musician musician = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO).build();
        Musician claimedMusician = musician.claim(userId);

        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.of(musician));
        when(musicianRepository.existsByUserId(userId)).thenReturn(false);
        when(musicianRepository.save(any())).thenReturn(claimedMusician);

        // When
        Musician result = commandService.claim(uuid, userId);

        // Then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.isClaimed()).isTrue();
        assertThat(result.getVerificationTier()).isEqualTo(VerificationTier.UNVERIFIED);
    }

    @Test
    @DisplayName("claim() - 실패: 존재하지 않는 uuid이면 MusicianNotFoundException(404) 발생")
    void claim_throwsException_whenMusicianNotFound() {
        // Given
        UUID uuid = UUID.randomUUID();
        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commandService.claim(uuid, 1L))
                .isInstanceOf(MusicianNotFoundException.class);
    }

    @Test
    @DisplayName("claim() - 실패: 이미 claim된 뮤지션이면 MusicianAlreadyClaimedException(409) 발생")
    void claim_throwsException_whenAlreadyClaimed() {
        // Given
        UUID uuid = UUID.randomUUID();
        Musician alreadyClaimed = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO)
                .userId(99L).build();
        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.of(alreadyClaimed));

        // When & Then
        assertThatThrownBy(() -> commandService.claim(uuid, 1L))
                .isInstanceOf(MusicianAlreadyClaimedException.class);
        verify(musicianRepository, never()).save(any());
    }

    @Test
    @DisplayName("claim() - 실패: 해당 userId가 이미 다른 뮤지션과 연결되어 있으면 MusicianUserAlreadyLinkedException(409) 발생")
    void claim_throwsException_whenUserAlreadyLinked() {
        // Given
        UUID uuid = UUID.randomUUID();
        Long userId = 42L;
        Musician musician = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO).build();
        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.of(musician));
        when(musicianRepository.existsByUserId(userId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> commandService.claim(uuid, userId))
                .isInstanceOf(MusicianUserAlreadyLinkedException.class);
        verify(musicianRepository, never()).save(any());
    }
}