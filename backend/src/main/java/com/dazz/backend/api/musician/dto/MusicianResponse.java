package com.dazz.backend.api.musician.dto;

import com.dazz.backend.domain.musician.Musician;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "뮤지션 기본 정보 응답")
public record MusicianResponse(
        @Schema(description = "뮤지션 내부 ID", example = "1")
        Long id,

        @Schema(description = "뮤지션 외부 식별자 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        String uuid,

        @Schema(description = "활동명", example = "김재즈")
        String stageName,

        @Schema(description = "주 포지션", example = "PIANO")
        String position,

        @Schema(description = "인증 등급: PUBLIC_PROFILE / UNVERIFIED / VERIFIED_USER / VERIFIED_PRO",
                example = "PUBLIC_PROFILE")
        String verificationTier,

        @Schema(description = "본인 계정 연결 여부", example = "false")
        boolean claimed
) {
    public static MusicianResponse from(Musician musician) {
        return new MusicianResponse(
                musician.getId(),
                musician.getUuid().toString(),
                musician.getStageName(),
                musician.getPosition().name(),
                musician.getVerificationTier().name(),
                musician.isClaimed()
        );
    }
}