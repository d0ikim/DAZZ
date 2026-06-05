package com.dazz.backend.application.collaboration.command;

import com.dazz.backend.domain.musician.RelationType;

public record CollaborationLinkCommand(
        Long fromMusicianId,
        Long toMusicianId,
        RelationType relationType
) {}