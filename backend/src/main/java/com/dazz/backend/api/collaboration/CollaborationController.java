package com.dazz.backend.api.collaboration;

import com.dazz.backend.api.collaboration.dto.CollaborationRequest;
import com.dazz.backend.api.collaboration.dto.CollaborationResponse;
import com.dazz.backend.api.common.ApiResponse;
import com.dazz.backend.api.common.ErrorResponse;
import com.dazz.backend.application.collaboration.CollaborationFacade;
import com.dazz.backend.application.collaboration.command.CollaborationLinkCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/collaborations")
@RequiredArgsConstructor
@Tag(name = "Collaboration", description = "협업 관계 등록 및 가중치 관리 API")
public class CollaborationController {

    private final CollaborationFacade collaborationFacade;

    @PostMapping
    @Operation(
            summary = "협업 관계 등록 / 가중치 증가",
            description = """
                    두 뮤지션 간의 협업 관계를 등록하거나, 이미 존재하는 경우 협업 횟수(weight)를 증가시킵니다.

                    **동시성**: Redisson 분산락으로 Lost Update를 방어합니다.

                    **멱등성**: `Idempotency-Key` 헤더(UUID)를 통해 중복 요청을 방어합니다.
                    - 동일 키 + 동일 페이로드 → 캐시된 응답 반환 (24h TTL)
                    - 동일 키 + 다른 페이로드 → 409 IDEMPOTENCY_CONFLICT (COM002)

                    **방향 정규화**: `fromMusicianId`와 `toMusicianId`의 순서에 관계없이
                    내부적으로 `min:max` 형태로 저장되어 중복이 방지됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "신규 협업 관계 생성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CollaborationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "기존 협업 관계 weight 증가 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CollaborationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "입력 검증 실패 또는 자기 자신과 협업 시도 (C002)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "뮤지션 없음 (M001)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = """
                    충돌 — 다음 중 하나:
                    - C003: 동시 요청 충돌 (잠시 후 재시도)
                    - COM002: 동일 Idempotency-Key + 다른 페이로드
                    """,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<CollaborationResponse>> link(
            @Parameter(description = "중복 요청 방지용 UUID (클라이언트 생성)",
                    example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,

            @Valid @RequestBody CollaborationRequest request
    ) {
        CollaborationLinkCommand command = new CollaborationLinkCommand(
                request.fromMusicianId(),
                request.toMusicianId(),
                request.relationType()
        );
        CollaborationResponse response = CollaborationResponse.from(
                collaborationFacade.linkOrIncrement(idempotencyKey, command));

        HttpStatus status = response.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.ok(response));
    }
}