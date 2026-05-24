package com.dazz.backend.api.musician.mapper;

import com.dazz.backend.api.musician.dto.MusicianInsightResponse;
import com.dazz.backend.application.musician.MusicianInsightResult;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.VerificationTier;

import java.util.List;

public class MusicianInsightMapper {

    private MusicianInsightMapper() {}

    public static MusicianInsightResponse toResponse(MusicianInsightResult result) {
        Musician musician = result.musician();

        MusicianInsightResponse.ProfileDto profile = new MusicianInsightResponse.ProfileDto(
                musician.getStageName(),
                musician.getPosition().name(),
                musician.getBio(),
                musician.getProfileImageUrl(),
                isVerified(musician.getVerificationTier())
        );

        List<MusicianInsightResponse.NetworkMemberDto> network = result.network().stream()
                .map(entry -> new MusicianInsightResponse.NetworkMemberDto(
                        entry.collaborator().getId(),
                        entry.collaborator().getStageName(),
                        entry.collaborator().getPosition().name(),
                        entry.relationType().name(),
                        entry.collaborationCount()
                ))
                .toList();

        return new MusicianInsightResponse(
                musician.getId(),
                profile,
                null, // docentNote: Post-MVP 구현 예정
                network
        );
    }

    private static boolean isVerified(VerificationTier tier) {
        return tier == VerificationTier.VERIFIED_USER || tier == VerificationTier.VERIFIED_PRO;
    }
}