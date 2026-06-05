package com.dazz.backend.api.musician;

import com.dazz.backend.api.common.ErrorResponse;
import com.dazz.backend.api.musician.dto.MusicianClaimRequest;
import com.dazz.backend.api.musician.dto.MusicianInsightResponse;
import com.dazz.backend.api.musician.dto.MusicianRegisterRequest;
import com.dazz.backend.api.musician.dto.MusicianResponse;
import com.dazz.backend.api.musician.mapper.MusicianInsightMapper;
import com.dazz.backend.application.musician.MusicianCommandService;
import com.dazz.backend.application.musician.MusicianFacade;
import com.dazz.backend.application.musician.MusicianInsightResult;
import com.dazz.backend.application.musician.MusicianQueryService;
import com.dazz.backend.application.musician.command.MusicianRegisterCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/musicians")
@RequiredArgsConstructor
@Validated
@Tag(name = "Musician", description = "뮤지션 프로필 및 인사이트 API")
public class MusicianController {

    private final MusicianQueryService musicianQueryService;
    private final MusicianCommandService musicianCommandService;
    private final MusicianFacade musicianFacade;

    // ── POST /api/v1/musicians ───────────────────────────────────────────────

    @PostMapping
    @Operation(
            summary = "뮤지션 Public Profile 등록",
            description = """
                    새로운 뮤지션의 Public Profile을 생성합니다.
                    - `stageName`과 `position`은 필수입니다.
                    - 등록 직후에는 `verificationTier = PUBLIC_PROFILE` 상태이며, 본인이 claim해야 `UNVERIFIED`로 승격됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MusicianResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력 검증 실패 — stageName 누락 또는 position 누락 (COM001)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.dazz.backend.api.common.ApiResponse<MusicianResponse>> register(
            @Valid @RequestBody MusicianRegisterRequest request
    ) {
        MusicianRegisterCommand command = new MusicianRegisterCommand(
                request.stageName(), request.realName(), request.position(),
                request.bio(), request.snsUrl(), request.profileImageUrl()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.dazz.backend.api.common.ApiResponse.ok(
                        MusicianResponse.from(musicianCommandService.register(command))));
    }

    // ── POST /api/v1/musicians/{uuid}/claim ──────────────────────────────────

    @PostMapping("/{uuid}/claim")
    @Operation(
            summary = "뮤지션 본인 계정 연결 (EC-01)",
            description = """
                    시스템에 사전 등록된 뮤지션 프로필에 본인 계정을 연결합니다.

                    **EC-01 제약**
                    - 한 User는 최대 하나의 Musician 프로필만 가질 수 있습니다.
                    - 이미 claim된 프로필에는 재시도할 수 없습니다.

                    **동시성**: Redisson 분산락으로 TOCTOU 경쟁 조건을 방어합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "claim 성공 — verificationTier가 UNVERIFIED로 승격",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MusicianResponse.class))),
            @ApiResponse(responseCode = "404", description = "뮤지션 없음 (M001)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = """
                    충돌 — 다음 중 하나:
                    - M002: 이미 다른 사용자가 claim한 뮤지션
                    - M003: 해당 userId가 이미 다른 뮤지션과 연결됨
                    - M004: 동시 요청 충돌 (잠시 후 재시도)
                    """,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.dazz.backend.api.common.ApiResponse<MusicianResponse>> claim(
            @Parameter(description = "클레임할 뮤지션의 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID uuid,
            @Valid @RequestBody MusicianClaimRequest request
    ) {
        return ResponseEntity.ok(
                com.dazz.backend.api.common.ApiResponse.ok(
                        MusicianResponse.from(musicianFacade.claim(uuid, request.userId()))));
    }

    // ── GET /api/v1/musicians/{musicianId}/insights ───────────────────────────

    @GetMapping("/{musicianId}/insights")
    @Operation(
            summary = "뮤지션 인사이트 조회",
            description = """
                    뮤지션의 **프로필 + 도슨트 노트 + 협업 네트워크**를 한 번에 조회합니다.

                    서비스의 핵심 API입니다. 성능 목표: p99 < 500ms.

                    - `includeNetwork=false`로 설정하면 네트워크 탐색을 생략하고 프로필만 반환합니다.
                    - `depth`는 협업 네트워크의 탐색 깊이입니다 (1 = 직접 협업자, 2 = 협업자의 협업자).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MusicianInsightResponse.class))),
            @ApiResponse(responseCode = "400", description = "depth 범위 초과 (1~2만 허용, COM001)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "뮤지션 없음 (M001)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.dazz.backend.api.common.ApiResponse<MusicianInsightResponse>> getInsight(
            @Parameter(description = "뮤지션 내부 ID", example = "102")
            @PathVariable Long musicianId,

            @Parameter(description = "협업 네트워크 포함 여부 (기본값: true)", example = "true")
            @RequestParam(defaultValue = "true") boolean includeNetwork,

            @Parameter(description = "네트워크 탐색 깊이 — 1 또는 2 (기본값: 1)", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) @Max(2) int depth
    ) {
        MusicianInsightResult result = musicianQueryService.getInsight(musicianId, includeNetwork, depth);
        return ResponseEntity.ok(com.dazz.backend.api.common.ApiResponse.ok(
                MusicianInsightMapper.toResponse(result)));
    }
}