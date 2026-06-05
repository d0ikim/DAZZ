package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

public class CollaborationDuplicateException extends BusinessException {

    public CollaborationDuplicateException(Long fromId, Long toId) {
        super(ErrorCode.COLLABORATION_DUPLICATE,
                "이미 존재하는 협업 관계입니다: " + fromId + " ↔ " + toId);
    }
}