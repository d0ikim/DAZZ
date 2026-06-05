package com.dazz.backend.api.musician.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "뮤지션 본인 계정 연결 요청 (EC-01)")
public record MusicianClaimRequest(
        @Schema(description = "연결할 사용자 ID (실서비스에서는 JWT에서 추출)", example = "42")
        @NotNull Long userId
) {}