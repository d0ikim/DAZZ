package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

import java.util.UUID;

/**
 * UNVERIFIED 상태가 아닌 뮤지션에 승인을 시도할 때 발생하는 예외.
 * EC-01 관리자 승인 정책 위반.
 */
public class MusicianNotApprovableException extends BusinessException {

    public MusicianNotApprovableException(UUID uuid) {
        super(ErrorCode.MUSICIAN_NOT_APPROVABLE,
                "승인할 수 없는 뮤지션입니다 (UNVERIFIED 상태가 아님): " + uuid);
    }
}