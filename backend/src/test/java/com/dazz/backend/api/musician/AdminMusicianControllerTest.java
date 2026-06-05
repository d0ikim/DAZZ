package com.dazz.backend.api.musician;

import com.dazz.backend.application.musician.AdminMusicianService;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.Position;
import com.dazz.backend.domain.musician.VerificationTier;
import com.dazz.backend.domain.musician.exception.MusicianNotApprovableException;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import com.dazz.backend.domain.musician.exception.MusicianNotRejectableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminMusicianController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
class AdminMusicianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminMusicianService adminMusicianService;

    // ── approve() Happy Path ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /admin/musicians/{uuid}/approve - 200: VERIFIED_USER로 승격 반환")
    void approve_returns200_withVerifiedUser() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        Musician approved = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO)
                .userId(42L).verificationTier(VerificationTier.VERIFIED_USER)
                .build();
        when(adminMusicianService.approve(uuid)).thenReturn(approved);

        // When & Then
        mockMvc.perform(post("/admin/musicians/{uuid}/approve", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationTier").value("VERIFIED_USER"))
                .andExpect(jsonPath("$.data.claimed").value(true));
    }

    // ── approve() Unhappy Path ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /admin/musicians/{uuid}/approve - 404: 뮤지션 없음 (M001)")
    void approve_returns404_whenMusicianNotFound() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        when(adminMusicianService.approve(uuid))
                .thenThrow(new MusicianNotFoundException(uuid));

        // When & Then
        mockMvc.perform(post("/admin/musicians/{uuid}/approve", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("M001"));
    }

    @Test
    @DisplayName("POST /admin/musicians/{uuid}/approve - 409: UNVERIFIED가 아닌 뮤지션 승인 시도 (M005)")
    void approve_returns409_whenNotApprovable() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        when(adminMusicianService.approve(uuid))
                .thenThrow(new MusicianNotApprovableException(uuid));

        // When & Then
        mockMvc.perform(post("/admin/musicians/{uuid}/approve", uuid))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("M005"));
    }

    // ── reject() Happy Path ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /admin/musicians/{uuid}/reject - 200: PUBLIC_PROFILE 복귀 + userId null 반환")
    void reject_returns200_withPublicProfile() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        Musician rejected = Musician.builder()
                .id(1L).uuid(uuid).stageName("김재즈").position(Position.PIANO)
                .verificationTier(VerificationTier.PUBLIC_PROFILE)
                .build(); // userId = null
        when(adminMusicianService.reject(uuid)).thenReturn(rejected);

        // When & Then
        mockMvc.perform(post("/admin/musicians/{uuid}/reject", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationTier").value("PUBLIC_PROFILE"))
                .andExpect(jsonPath("$.data.claimed").value(false));
    }

    // ── reject() Unhappy Path ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /admin/musicians/{uuid}/reject - 404: 뮤지션 없음 (M001)")
    void reject_returns404_whenMusicianNotFound() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        when(adminMusicianService.reject(uuid))
                .thenThrow(new MusicianNotFoundException(uuid));

        // When & Then
        mockMvc.perform(post("/admin/musicians/{uuid}/reject", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("M001"));
    }

    @Test
    @DisplayName("POST /admin/musicians/{uuid}/reject - 409: UNVERIFIED가 아닌 뮤지션 거절 시도 (M006)")
    void reject_returns409_whenNotRejectable() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        when(adminMusicianService.reject(uuid))
                .thenThrow(new MusicianNotRejectableException(uuid));

        // When & Then
        mockMvc.perform(post("/admin/musicians/{uuid}/reject", uuid))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("M006"));
    }
}