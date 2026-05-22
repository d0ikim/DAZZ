package com.dazz.backend.domain.musician;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 뮤지션 Aggregate Root.
 * - uuid: 동명이인 오염 방지용 외부 식별자 (EC-02)
 * - userId: Optional — 시스템 선등록 후 본인이 나타나 연결하는 구조
 * - verificationTier: 신뢰 등급 (EC-01)
 */
@Getter
public class Musician {

    private final Long id;
    private final UUID uuid;
    private final Long userId;
    private final String stageName;
    private final String realName;
    private final Position position;
    private final String bio;
    private final String snsUrl;
    private final String profileImageUrl;
    private final VerificationTier verificationTier;
    private final LocalDateTime createdAt;

    @Builder
    private Musician(Long id, UUID uuid, Long userId, String stageName, String realName,
                     Position position, String bio, String snsUrl, String profileImageUrl,
                     VerificationTier verificationTier, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid != null ? uuid : UUID.randomUUID();
        this.userId = userId;
        this.stageName = stageName;
        this.realName = realName;
        this.position = position;
        this.bio = bio;
        this.snsUrl = snsUrl;
        this.profileImageUrl = profileImageUrl;
        this.verificationTier = verificationTier != null ? verificationTier : VerificationTier.PUBLIC_PROFILE;
        this.createdAt = createdAt;
    }

    /**
     * 사용자가 본인 계정을 뮤지션 프로필에 연결할 때 사용.
     * userId가 이미 존재하면 호출부에서 예외를 던져야 한다.
     */
    public Musician claim(Long userId) {
        return Musician.builder()
                .id(this.id)
                .uuid(this.uuid)
                .userId(userId)
                .stageName(this.stageName)
                .realName(this.realName)
                .position(this.position)
                .bio(this.bio)
                .snsUrl(this.snsUrl)
                .profileImageUrl(this.profileImageUrl)
                .verificationTier(VerificationTier.UNVERIFIED)
                .createdAt(this.createdAt)
                .build();
    }

    public boolean isClaimed() {
        return this.userId != null;
    }
}
