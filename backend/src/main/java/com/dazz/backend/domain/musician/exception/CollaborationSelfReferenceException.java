package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

public class CollaborationSelfReferenceException extends BusinessException {

    public CollaborationSelfReferenceException(Long musicianId) {
        super(ErrorCode.COLLABORATION_SELF_REFERENCE,
                "자기 자신과 협업 관계를 맺을 수 없습니다: " + musicianId);
    }
}