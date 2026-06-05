package com.dazz.backend.api.musician.dto;

import com.dazz.backend.domain.musician.Position;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MusicianRegisterRequest(
        @NotBlank String stageName,
        String realName,
        @NotNull Position position,
        String bio,
        String snsUrl,
        String profileImageUrl
) {}