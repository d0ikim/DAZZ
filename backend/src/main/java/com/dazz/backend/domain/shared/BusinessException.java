package com.dazz.backend.domain.shared;

/**
 * 모든 도메인 예외의 부모 클래스.
 * throw new RuntimeException() 대신 이 계층을 상속한 구체 예외를 사용한다.
 */
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
