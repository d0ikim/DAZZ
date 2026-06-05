package com.dazz.backend.api.musician;

import com.dazz.backend.application.musician.MusicianInsightResult;
import com.dazz.backend.application.musician.MusicianQueryService;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.Position;
import com.dazz.backend.domain.musician.RelationType;
import com.dazz.backend.domain.musician.VerificationTier;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = MusicianController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
class MusicianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MusicianQueryService musicianQueryService;

    // ── Happy Path ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/musicians/{id}/insights - 200: 프로필 + 네트워크 반환")
    void getInsight_returns200_withProfileAndNetwork() throws Exception {
        // Given
        Musician musician = Musician.builder()
                .id(102L)
                .stageName("김재즈")
                .position(Position.PIANO)
                .bio("서울예대 출신으로 정통비밥의 계보를 잇는 연주자입니다.")
                .verificationTier(VerificationTier.VERIFIED_PRO)
                .build();
        Musician collaborator = Musician.builder()
                .id(205L)
                .stageName("이재즈")
                .position(Position.BASS)
                .verificationTier(VerificationTier.VERIFIED_USER)
                .build();
        MusicianInsightResult result = new MusicianInsightResult(
                musician,
                List.of(new MusicianInsightResult.NetworkEntry(collaborator, RelationType.COLLABORATION, 15))
        );
        when(musicianQueryService.getInsight(102L, true, 1)).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/v1/musicians/102/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.musicianId").value(102))
                .andExpect(jsonPath("$.data.profile.stageName").value("김재즈"))
                .andExpect(jsonPath("$.data.profile.position").value("PIANO"))
                .andExpect(jsonPath("$.data.profile.isVerified").value(true))
                .andExpect(jsonPath("$.data.network[0].targetId").value(205))
                .andExpect(jsonPath("$.data.network[0].name").value("이재즈"))
                .andExpect(jsonPath("$.data.network[0].relationType").value("COLLABORATION"))
                .andExpect(jsonPath("$.data.network[0].collaborationCount").value(15));
    }

    @Test
    @DisplayName("GET /api/v1/musicians/{id}/insights?includeNetwork=false - 200: 네트워크 빈 배열 반환")
    void getInsight_returns200_withEmptyNetworkWhenIncludeNetworkFalse() throws Exception {
        // Given
        Musician musician = Musician.builder()
                .id(102L)
                .stageName("김재즈")
                .position(Position.PIANO)
                .verificationTier(VerificationTier.VERIFIED_PRO)
                .build();
        MusicianInsightResult result = new MusicianInsightResult(musician, List.of());
        when(musicianQueryService.getInsight(102L, false, 1)).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/v1/musicians/102/insights?includeNetwork=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.musicianId").value(102))
                .andExpect(jsonPath("$.data.network").isEmpty());
    }

    // ── Unhappy Path ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/musicians/{id}/insights - 404: 존재하지 않는 뮤지션")
    void getInsight_returns404_whenMusicianNotFound() throws Exception {
        // Given
        when(musicianQueryService.getInsight(999L, true, 1))
                .thenThrow(new MusicianNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/v1/musicians/999/insights"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("M001"));
    }

    @Test
    @DisplayName("GET /api/v1/musicians/{id}/insights?depth=3 - 400: depth 범위 초과")
    void getInsight_returns400_whenDepthIsOutOfRange() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/musicians/102/insights?depth=3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COM001"));
    }
}
