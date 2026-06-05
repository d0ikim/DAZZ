package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

public class CollaborationConcurrentException extends BusinessException {

    public CollaborationConcurrentException(Long aId, Long bId) {
        super(ErrorCode.COLLABORATION_CONCURRENT,
                "다른 요청이 진행 중입니다. 잠시 후 다시 시도하세요: " + aId + " ↔ " + bId);
    }
}