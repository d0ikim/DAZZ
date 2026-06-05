package com.dazz.backend.api.musician;

import com.dazz.backend.api.common.ApiResponse;
import com.dazz.backend.api.common.ErrorResponse;
import com.dazz.backend.api.musician.dto.MusicianResponse;
import com.dazz.backend.application.musician.AdminMusicianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/musicians")
@RequiredArgsConstructor
@Tag(name = "Admin - Musician", description = "관리자 뮤지션 승인/거절 API")
public class AdminMusicianController {

    private final AdminMusicianService adminMusicianService;

    // ── POST /admin/musicians/{uuid}/approve ─────────────────────────────────

    @PostMapping("/{uuid}/approve")
    @Operation(
            summary = "뮤지션 승인 (EC-01)",
            description = """
                    UNVERIFIED 상태의 뮤지션을 VERIFIED_USER로 승격합니다.

                    **EC-01 제약**
                    - UNVERIFIED 상태인 뮤지션에만 적용 가능합니다.
                    - 이미 VERIFIED_USER 또는 VERIFIED_PRO인 뮤지션에는 승인 불가 (M005).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "승인 성공 — verificationTier가 VERIFIED_USER로 승격",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MusicianResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "뮤지션 없음 (M001)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "승인 불가 상태 — UNVERIFIED가 아닌 뮤지션 (M005)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<MusicianResponse>> approve(
            @Parameter(description = "승인할 뮤지션의 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID uuid
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(MusicianResponse.from(adminMusicianService.approve(uuid))));
    }

    // ── POST /admin/musicians/{uuid}/reject ──────────────────────────────────

    @PostMapping("/{uuid}/reject")
    @Operation(
            summary = "뮤지션 거절 (EC-01)",
            description = """
                    UNVERIFIED 상태의 뮤지션을 PUBLIC_PROFILE로 복귀시키고 userId 연결을 해제합니다.

                    **EC-01 제약**
                    - UNVERIFIED 상태인 뮤지션에만 적용 가능합니다.
                    - 이미 PUBLIC_PROFILE(claim 안 된) 상태에는 거절 불가 (M006).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "거절 성공 — verificationTier가 PUBLIC_PROFILE로 복귀, userId가 null로 초기화",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MusicianResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "뮤지션 없음 (M001)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "거절 불가 상태 — UNVERIFIED가 아닌 뮤지션 (M006)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<MusicianResponse>> reject(
            @Parameter(description = "거절할 뮤지션의 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID uuid
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(MusicianResponse.from(adminMusicianService.reject(uuid))));
    }
}