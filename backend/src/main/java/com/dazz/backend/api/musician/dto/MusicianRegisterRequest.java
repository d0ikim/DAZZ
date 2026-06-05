package com.dazz.backend.api.musician.dto;

import com.dazz.backend.domain.musician.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "뮤지션 Public Profile 등록 요청")
public record MusicianRegisterRequest(
        @Schema(description = "활동명 (필수)", example = "김재즈")
        @NotBlank String stageName,

        @Schema(description = "본명 (선택)", example = "김도이")
        String realName,

        @Schema(description = "주 포지션 (필수): PIANO, BASS, DRUMS, GUITAR, SAXOPHONE, TRUMPET, VIOLIN, VOCAL, ETC",
                example = "PIANO")
        @NotNull Position position,

        @Schema(description = "소개글 (선택)", example = "서울예대 출신으로 정통 비밥의 계보를 잇는 연주자입니다.")
        String bio,

        @Schema(description = "SNS 링크 (선택)", example = "https://instagram.com/kimjazz")
        String snsUrl,

        @Schema(description = "프로필 이미지 URL (선택)", example = "https://cdn.dazz.kr/profiles/kimjazz.jpg")
        String profileImageUrl
) {}