package com.dazz.backend.application.musician;

import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.RelationType;

import java.util.List;

public record MusicianInsightResult(
        Musician musician,
        List<NetworkEntry> network
) {
    public record NetworkEntry(
            Musician collaborator,
            RelationType relationType,
            int collaborationCount
    ) {}
}