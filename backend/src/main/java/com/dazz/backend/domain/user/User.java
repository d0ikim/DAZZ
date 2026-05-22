package com.dazz.backend.domain.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 서비스 사용자 도메인 객체.
 * passwordHash: BCrypt 해시값. 도메인 레이어는 해싱 로직을 모른다 (인프라 책임).
 * nickname: ERD 기준 필드명 유지.
 */
@Getter
public class User {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String nickname;
    private final UserRole role;
    private final LocalDateTime createdAt;

    @Builder
    private User(Long id, String email, String passwordHash, String nickname,
                 UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.role = role != null ? role : UserRole.USER;
        this.createdAt = createdAt;
    }
}
