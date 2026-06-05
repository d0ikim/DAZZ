package com.dazz.backend.domain.musician;

import com.dazz.backend.domain.musician.exception.MusicianAlreadyClaimedException;
import com.dazz.backend.domain.musician.exception.MusicianInvalidException;
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
     * 신규 Public Profile 생성. stageName과 position은 필수 항목.
     */
    public static Musician register(String stageName, String realName, Position position,
                                    String bio, String snsUrl, String profileImageUrl) {
        if (stageName == null || stageName.isBlank()) {
            throw new MusicianInvalidException("stageName은 필수입니다.");
        }
        if (position == null) {
            throw new MusicianInvalidException("position은 필수입니다.");
        }
        return Musician.builder()
                .stageName(stageName)
                .realName(realName)
                .position(position)
                .bio(bio)
                .snsUrl(snsUrl)
                .profileImageUrl(profileImageUrl)
                .build();
    }

    /**
     * 사용자가 본인 계정을 뮤지션 프로필에 연결할 때 사용.
     * 이미 claim된 프로필에 재시도하면 도메인이 직접 예외를 던진다.
     */
    public Musician claim(Long userId) {
        if (this.isClaimed()) {
            throw new MusicianAlreadyClaimedException(this.uuid);
        }
        return new Musician(this.id, this.uuid, userId, this.stageName, this.realName,
                this.position, this.bio, this.snsUrl, this.profileImageUrl,
                VerificationTier.UNVERIFIED, this.createdAt);
    }

    public boolean isClaimed() {
        return this.userId != null;
    }
}
