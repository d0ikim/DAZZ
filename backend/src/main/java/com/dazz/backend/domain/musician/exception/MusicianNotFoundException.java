package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

public class MusicianNotFoundException extends BusinessException {

    public MusicianNotFoundException(Long musicianId) {
        super(ErrorCode.MUSICIAN_NOT_FOUND, "뮤지션을 찾을 수 없습니다: " + musicianId);
    }

    public MusicianNotFoundException(java.util.UUID uuid) {
        super(ErrorCode.MUSICIAN_NOT_FOUND, "뮤지션을 찾을 수 없습니다: " + uuid);
    }
}
