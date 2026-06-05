package com.dazz.backend.api.musician.dto;

import com.dazz.backend.domain.musician.Musician;

public record MusicianResponse(
        Long id,
        String uuid,
        String stageName,
        String position,
        String verificationTier,
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