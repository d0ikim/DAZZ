package com.dazz.backend.domain.album;

/**
 * 앨범 참여 역할. ERD의 ALBUM_PARTICIPATION.participation_type에 대응.
 */
public enum ParticipationType {
    LEADER,    // 리더/앨범 주인공
    SIDEMAN,   // 세션 연주자
    COMPOSER   // 작/편곡자
}
