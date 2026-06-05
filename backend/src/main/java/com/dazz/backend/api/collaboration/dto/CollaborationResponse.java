package com.dazz.backend.api.collaboration.dto;

import com.dazz.backend.application.collaboration.CollaborationResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "협업 관계 등록/가중치 증가 응답")
public record CollaborationResponse(

        @Schema(description = "협업 관계 내부 ID", example = "1")
        Long id,

        @Schema(description = "뮤지션 A 내부 ID (정규화: min)", example = "102")
        Long fromMusicianId,

        @Schema(description = "뮤지션 B 내부 ID (정규화: max)", example = "205")
        Long toMusicianId,

        @Schema(description = "협업 관계 유형", example = "COLLABORATION")
        String relationType,

        @Schema(description = "누적 협업 횟수(가중치)", example = "3")
        int weight,

        @Schema(description = "신규 생성 여부. false이면 기존 관계의 weight가 증가된 것", example = "true")
        boolean created
) {
    public static CollaborationResponse from(CollaborationResult result) {
        return new CollaborationResponse(
                result.id(),
                result.fromMusicianId(),
                result.toMusicianId(),
                result.relationType().name(),
                result.weight(),
                result.created()
        );
    }
}