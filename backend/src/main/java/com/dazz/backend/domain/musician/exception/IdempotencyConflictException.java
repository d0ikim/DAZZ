package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

public class IdempotencyConflictException extends BusinessException {

    public IdempotencyConflictException(String key) {
        super(ErrorCode.IDEMPOTENCY_CONFLICT,
                "동일한 Idempotency-Key로 다른 페이로드가 요청되었습니다: " + key);
    }
}