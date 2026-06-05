package com.dazz.backend.domain.shared;

/**
 * 도메인 에러 코드. int httpStatus를 사용해 domain이 Spring에 의존하지 않도록 한다.
 */
public enum ErrorCode {

    MUSICIAN_NOT_FOUND(404, "M001", "뮤지션을 찾을 수 없습니다."),
    MUSICIAN_ALREADY_CLAIMED(409, "M002", "이미 본인 인증된 뮤지션입니다."),
    MUSICIAN_USER_ALREADY_LINKED(409, "M003", "해당 사용자는 이미 다른 뮤지션과 연결되어 있습니다."),
    MUSICIAN_CLAIM_CONFLICT(409, "M004", "다른 요청이 진행 중입니다. 잠시 후 다시 시도하세요."),

    ALBUM_NOT_FOUND(404, "A001", "앨범을 찾을 수 없습니다."),
    ALBUM_PARTICIPATION_DUPLICATE(409, "A002", "이미 등록된 앨범 참여 정보입니다."),

    COLLABORATION_DUPLICATE(409, "C001", "이미 존재하는 협업 관계입니다."),
    COLLABORATION_SELF_REFERENCE(400, "C002", "자기 자신과 협업 관계를 맺을 수 없습니다."),
    COLLABORATION_CONCURRENT(409, "C003", "다른 요청이 진행 중입니다. 잠시 후 다시 시도하세요."),

    GROUP_NOT_FOUND(404, "G001", "그룹을 찾을 수 없습니다."),
    GROUP_MEMBER_DUPLICATE(409, "G002", "이미 해당 그룹의 멤버입니다."),

    PERFORMANCE_NOT_FOUND(404, "P001", "공연을 찾을 수 없습니다."),
    CLUB_NOT_FOUND(404, "P002", "클럽(공연장)을 찾을 수 없습니다."),

    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
    USER_EMAIL_DUPLICATE(409, "U002", "이미 사용 중인 이메일입니다."),

    INVALID_INPUT(400, "COM001", "잘못된 입력입니다."),
    IDEMPOTENCY_CONFLICT(409, "COM002", "동일한 Idempotency-Key로 다른 페이로드가 요청되었습니다."),
    INTERNAL_SERVER_ERROR(500, "COM999", "서버 내부 오류입니다.");

    private final int httpStatus;
    private final String code;
    private final String message;

    ErrorCode(int httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public int getHttpStatus() { return httpStatus; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
