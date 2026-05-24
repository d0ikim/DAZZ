package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

import java.util.UUID;

public class MusicianAlreadyClaimedException extends BusinessException {

    public MusicianAlreadyClaimedException(UUID uuid) {
        super(ErrorCode.MUSICIAN_ALREADY_CLAIMED, "이미 본인 인증된 뮤지션입니다: " + uuid);
    }
}
