package com.dazz.backend.application.musician.command;

import com.dazz.backend.domain.musician.Position;

public record MusicianRegisterCommand(
        String stageName,
        String realName,
        Position position,
        String bio,
        String snsUrl,
        String profileImageUrl
) {}
