package com.dazz.backend.api.musician.dto;

import jakarta.validation.constraints.NotNull;

public record MusicianClaimRequest(@NotNull Long userId) {}