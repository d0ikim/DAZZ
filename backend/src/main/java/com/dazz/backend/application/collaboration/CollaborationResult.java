package com.dazz.backend.application.collaboration;

import com.dazz.backend.domain.musician.Collaboration;
import com.dazz.backend.domain.musician.RelationType;

public record CollaborationResult(
        Long id,
        Long fromMusicianId,
        Long toMusicianId,
        RelationType relationType,
        int weight,
        boolean created
) {
    public static CollaborationResult of(Collaboration collab, boolean created) {
        return new CollaborationResult(
                collab.getId(),
                collab.getFromMusicianId(),
                collab.getToMusicianId(),
                collab.getRelationType(),
                collab.getWeight(),
                created
        );
    }
}