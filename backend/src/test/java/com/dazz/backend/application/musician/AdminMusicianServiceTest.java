package com.dazz.backend.application.musician;

import com.dazz.backend.application.port.out.MusicianRepository;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.Position;
import com.dazz.backend.domain.musician.VerificationTier;
import com.dazz.backend.domain.musician.exception.MusicianNotApprovableException;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import com.dazz.backend.domain.musician.exception.MusicianNotRejectableException;
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
@DisplayName("AdminMusicianService 단위 테스트")
class AdminMusicianServiceTest {

    @Mock
    private MusicianRepository musicianRepository;

    @InjectMocks
    private AdminMusicianService adminMusicianService;

    // ── approve() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("approve() - 성공: UNVERIFIED 뮤지션이 VERIFIED_USER로 승격된다")
    void approve_success() {
        // Given
        UUID uuid = UUID.randomUUID();
        Musician unverified = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO)
                .userId(42L).verificationTier(VerificationTier.UNVERIFIED)
                .build();
        Musician approvedMusician = unverified.approve();

        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.of(unverified));
        when(musicianRepository.save(any())).thenReturn(approvedMusician);

        // When
        Musician result = adminMusicianService.approve(uuid);

        // Then
        assertThat(result.getVerificationTier()).isEqualTo(VerificationTier.VERIFIED_USER);
        assertThat(result.getUserId()).isEqualTo(42L);
        verify(musicianRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("approve() - 실패: 존재하지 않는 uuid이면 MusicianNotFoundException(404) 발생")
    void approve_throwsException_whenMusicianNotFound() {
        // Given
        UUID uuid = UUID.randomUUID();
        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminMusicianService.approve(uuid))
                .isInstanceOf(MusicianNotFoundException.class);
        verify(musicianRepository, never()).save(any());
    }

    @Test
    @DisplayName("approve() - 실패: VERIFIED_USER 뮤지션에 승인 시도 시 MusicianNotApprovableException(409) 발생")
    void approve_throwsException_whenAlreadyVerified() {
        // Given
        UUID uuid = UUID.randomUUID();
        Musician alreadyVerified = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO)
                .userId(42L).verificationTier(VerificationTier.VERIFIED_USER)
                .build();
        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.of(alreadyVerified));

        // When & Then
        assertThatThrownBy(() -> adminMusicianService.approve(uuid))
                .isInstanceOf(MusicianNotApprovableException.class);
        verify(musicianRepository, never()).save(any());
    }

    @Test
    @DisplayName("approve() - 실패: PUBLIC_PROFILE 뮤지션에 승인 시도 시 MusicianNotApprovableException(409) 발생")
    void approve_throwsException_whenPublicProfile() {
        // Given
        UUID uuid = UUID.randomUUID();
        Musician publicProfile = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO)
                .build(); // verificationTier 기본값 = PUBLIC_PROFILE
        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.of(publicProfile));

        // When & Then
        assertThatThrownBy(() -> adminMusicianService.approve(uuid))
                .isInstanceOf(MusicianNotApprovableException.class);
        verify(musicianRepository, never()).save(any());
    }

    // ── reject() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("reject() - 성공: UNVERIFIED 뮤지션이 PUBLIC_PROFILE로 복귀하고 userId가 null이 된다")
    void reject_success() {
        // Given
        UUID uuid = UUID.randomUUID();
        Musician unverified = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO)
                .userId(42L).verificationTier(VerificationTier.UNVERIFIED)
                .build();
        Musician rejectedMusician = unverified.reject();

        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.of(unverified));
        when(musicianRepository.save(any())).thenReturn(rejectedMusician);

        // When
        Musician result = adminMusicianService.reject(uuid);

        // Then
        assertThat(result.getVerificationTier()).isEqualTo(VerificationTier.PUBLIC_PROFILE);
        assertThat(result.getUserId()).isNull();
        assertThat(result.isClaimed()).isFalse();
        verify(musicianRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("reject() - 실패: 존재하지 않는 uuid이면 MusicianNotFoundException(404) 발생")
    void reject_throwsException_whenMusicianNotFound() {
        // Given
        UUID uuid = UUID.randomUUID();
        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adminMusicianService.reject(uuid))
                .isInstanceOf(MusicianNotFoundException.class);
        verify(musicianRepository, never()).save(any());
    }

    @Test
    @DisplayName("reject() - 실패: PUBLIC_PROFILE 뮤지션에 거절 시도 시 MusicianNotRejectableException(409) 발생")
    void reject_throwsException_whenPublicProfile() {
        // Given
        UUID uuid = UUID.randomUUID();
        Musician publicProfile = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO)
                .build(); // verificationTier 기본값 = PUBLIC_PROFILE
        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.of(publicProfile));

        // When & Then
        assertThatThrownBy(() -> adminMusicianService.reject(uuid))
                .isInstanceOf(MusicianNotRejectableException.class);
        verify(musicianRepository, never()).save(any());
    }

    @Test
    @DisplayName("reject() - 실패: VERIFIED_USER 뮤지션에 거절 시도 시 MusicianNotRejectableException(409) 발생")
    void reject_throwsException_whenVerifiedUser() {
        // Given
        UUID uuid = UUID.randomUUID();
        Musician verifiedUser = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO)
                .userId(42L).verificationTier(VerificationTier.VERIFIED_USER)
                .build();
        when(musicianRepository.findByUuid(uuid)).thenReturn(Optional.of(verifiedUser));

        // When & Then
        assertThatThrownBy(() -> adminMusicianService.reject(uuid))
                .isInstanceOf(MusicianNotRejectableException.class);
        verify(musicianRepository, never()).save(any());
    }
}