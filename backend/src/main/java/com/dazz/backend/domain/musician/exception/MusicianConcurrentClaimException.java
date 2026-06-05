package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

import java.util.UUID;

public class MusicianConcurrentClaimException extends BusinessException {

    public MusicianConcurrentClaimException(UUID uuid) {
        super(ErrorCode.MUSICIAN_CLAIM_CONFLICT, "다른 요청이 진행 중입니다: " + uuid);
    }
}