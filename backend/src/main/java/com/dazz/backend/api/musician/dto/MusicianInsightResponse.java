package com.dazz.backend.api.musician.dto;

import java.util.List;

public record MusicianInsightResponse(
        Long musicianId,
        ProfileDto profile,
        DocentNoteDto docentNote,
        List<NetworkMemberDto> network
) {
    public record ProfileDto(
            String stageName,
            String position,
            String bio,
            String profileImageUrl,
            boolean isVerified
    ) {}

    public record DocentNoteDto(
            List<String> styleTags,
            String summary
    ) {}

    public record NetworkMemberDto(
            Long targetId,
            String name,
            String instrument,
            String relationType,
            int collaborationCount
    ) {}
}