package com.dazz.backend.domain.musician.exception;

import com.dazz.backend.domain.shared.BusinessException;
import com.dazz.backend.domain.shared.ErrorCode;

public class MusicianUserAlreadyLinkedException extends BusinessException {

    public MusicianUserAlreadyLinkedException(Long userId) {
        super(ErrorCode.MUSICIAN_USER_ALREADY_LINKED, "이미 뮤지션 프로필이 연결된 사용자입니다: " + userId);
    }
}
