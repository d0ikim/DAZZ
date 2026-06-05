package com.dazz.backend.api.musician.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "뮤지션 인사이트 응답 (프로필 + 도슨트 노트 + 협업 네트워크)")
public record MusicianInsightResponse(
        @Schema(description = "뮤지션 내부 ID", example = "102")
        Long musicianId,

        @Schema(description = "뮤지션 프로필")
        ProfileDto profile,

        @Schema(description = "도슨트 노트 (큐레이터 작성 인사이트). 미작성 시 null")
        DocentNoteDto docentNote,

        @Schema(description = "협업 네트워크. includeNetwork=false 시 빈 배열")
        List<NetworkMemberDto> network
) {
    @Schema(description = "뮤지션 프로필 요약")
    public record ProfileDto(
            @Schema(description = "활동명", example = "김재즈")
            String stageName,

            @Schema(description = "주 포지션", example = "PIANO")
            String position,

            @Schema(description = "소개글", example = "서울예대 출신으로 정통 비밥의 계보를 잇는 연주자입니다.")
            String bio,

            @Schema(description = "프로필 이미지 URL", example = "https://cdn.dazz.kr/profiles/kimjazz.jpg")
            String profileImageUrl,

            @Schema(description = "VERIFIED_USER 이상 등급 여부", example = "true")
            boolean isVerified
    ) {}

    @Schema(description = "도슨트 노트 — 큐레이터가 작성한 뮤지션 인사이트")
    public record DocentNoteDto(
            @Schema(description = "스타일 태그 목록", example = "[\"비밥\", \"포스트밥\", \"모달재즈\"]")
            List<String> styleTags,

            @Schema(description = "한 줄 요약", example = "한국 재즈 피아노의 정통 계보를 잇는 연주자")
            String summary
    ) {}

    @Schema(description = "협업 네트워크 멤버 항목")
    public record NetworkMemberDto(
            @Schema(description = "협업 뮤지션 내부 ID", example = "205")
            Long targetId,

            @Schema(description = "협업 뮤지션 활동명", example = "이재즈")
            String name,

            @Schema(description = "협업 뮤지션 포지션", example = "BASS")
            String instrument,

            @Schema(description = "협업 관계 타입: COLLABORATION / MENTOR_MENTEE / BANDMATE",
                    example = "COLLABORATION")
            String relationType,

            @Schema(description = "협업 횟수 (가중치)", example = "15")
            int collaborationCount
    ) {}
}