package com.dazz.backend.api.collaboration;

import com.dazz.backend.application.collaboration.CollaborationFacade;
import com.dazz.backend.application.collaboration.CollaborationResult;
import com.dazz.backend.application.collaboration.command.CollaborationLinkCommand;
import com.dazz.backend.domain.musician.RelationType;
import com.dazz.backend.domain.musician.exception.CollaborationConcurrentException;
import com.dazz.backend.domain.musician.exception.CollaborationSelfReferenceException;
import com.dazz.backend.domain.musician.exception.IdempotencyConflictException;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CollaborationController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@DisplayName("CollaborationController 단위 테스트")
class CollaborationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CollaborationFacade collaborationFacade;

    private static final String IDEMPOTENCY_KEY = "550e8400-e29b-41d4-a716-446655440000";
    private static final String BASE_BODY = """
            {"fromMusicianId": 102, "toMusicianId": 205, "relationType": "COLLABORATION"}
            """;

    // ── Happy Path ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/collaborations - 201: 신규 협업 관계 생성")
    void link_returns201_whenNewCollaboration() throws Exception {
        CollaborationResult result = new CollaborationResult(1L, 102L, 205L, RelationType.COLLABORATION, 1, true);
        when(collaborationFacade.linkOrIncrement(eq(IDEMPOTENCY_KEY), any(CollaborationLinkCommand.class)))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/collaborations")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BASE_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.weight").value(1))
                .andExpect(jsonPath("$.data.created").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/collaborations - 200: 기존 협업 관계 weight 증가")
    void link_returns200_whenExistingCollaboration() throws Exception {
        CollaborationResult result = new CollaborationResult(1L, 102L, 205L, RelationType.COLLABORATION, 4, false);
        when(collaborationFacade.linkOrIncrement(eq(IDEMPOTENCY_KEY), any(CollaborationLinkCommand.class)))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/collaborations")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BASE_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.weight").value(4))
                .andExpect(jsonPath("$.data.created").value(false));
    }

    // ── Unhappy Path ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/collaborations - 400: Idempotency-Key 헤더 누락")
    void link_returns400_whenMissingIdempotencyKey() throws Exception {
        mockMvc.perform(post("/api/v1/collaborations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BASE_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COM001"));
    }

    @Test
    @DisplayName("POST /api/v1/collaborations - 400: 자기 자신과 협업 시도 (C002)")
    void link_returns400_whenSelfReference() throws Exception {
        when(collaborationFacade.linkOrIncrement(any(), any()))
                .thenThrow(new CollaborationSelfReferenceException(102L));

        mockMvc.perform(post("/api/v1/collaborations")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BASE_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C002"));
    }

    @Test
    @DisplayName("POST /api/v1/collaborations - 404: 뮤지션 없음 (M001)")
    void link_returns404_whenMusicianNotFound() throws Exception {
        when(collaborationFacade.linkOrIncrement(any(), any()))
                .thenThrow(new MusicianNotFoundException(102L));

        mockMvc.perform(post("/api/v1/collaborations")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BASE_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("M001"));
    }

    @Test
    @DisplayName("POST /api/v1/collaborations - 409: 동시 요청 충돌 (C003)")
    void link_returns409_whenConcurrentConflict() throws Exception {
        when(collaborationFacade.linkOrIncrement(any(), any()))
                .thenThrow(new CollaborationConcurrentException(102L, 205L));

        mockMvc.perform(post("/api/v1/collaborations")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BASE_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("C003"));
    }

    @Test
    @DisplayName("POST /api/v1/collaborations - 409: 동일 키 + 다른 페이로드 (COM002)")
    void link_returns409_whenIdempotencyConflict() throws Exception {
        when(collaborationFacade.linkOrIncrement(any(), any()))
                .thenThrow(new IdempotencyConflictException(IDEMPOTENCY_KEY));

        mockMvc.perform(post("/api/v1/collaborations")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BASE_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("COM002"));
    }
}