package com.dazz.backend.api.collaboration.dto;

import com.dazz.backend.domain.musician.RelationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "협업 관계 등록/가중치 증가 요청")
public record CollaborationRequest(

        @Schema(description = "협업 뮤지션 A의 내부 ID", example = "102")
        @NotNull(message = "fromMusicianId는 필수입니다.")
        Long fromMusicianId,

        @Schema(description = "협업 뮤지션 B의 내부 ID", example = "205")
        @NotNull(message = "toMusicianId는 필수입니다.")
        Long toMusicianId,

        @Schema(description = "협업 관계 유형: COLLABORATION / MENTOR / BAND_MEMBER",
                example = "COLLABORATION")
        @NotNull(message = "relationType은 필수입니다.")
        RelationType relationType
) {}