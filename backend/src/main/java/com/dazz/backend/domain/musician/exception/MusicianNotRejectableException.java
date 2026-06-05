package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

import java.util.UUID;

/**
 * UNVERIFIED 상태가 아닌 뮤지션에 거절을 시도할 때 발생하는 예외.
 * EC-01 관리자 거절 정책 위반.
 */
public class MusicianNotRejectableException extends BusinessException {

    public MusicianNotRejectableException(UUID uuid) {
        super(ErrorCode.MUSICIAN_NOT_REJECTABLE,
                "거절할 수 없는 뮤지션입니다 (UNVERIFIED 상태가 아님): " + uuid);
    }
}