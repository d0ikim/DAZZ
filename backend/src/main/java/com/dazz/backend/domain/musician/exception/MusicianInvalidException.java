package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

public class MusicianInvalidException extends BusinessException {

    public MusicianInvalidException(String message) {
        super(ErrorCode.INVALID_INPUT, message);
    }
}