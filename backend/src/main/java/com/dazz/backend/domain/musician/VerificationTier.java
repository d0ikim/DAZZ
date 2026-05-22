package com.dazz.backend.domain.musician;

/**
 * 뮤지션 신뢰 등급 (EC-01: 허위 프로필 선점 방지).
 * docs/01-requirements.md Section 4 Trust Tier 매트릭스 기준.
 * PUBLIC_PROFILE → UNVERIFIED → VERIFIED_USER → VERIFIED_PRO 순으로 승격된다.
 */
public enum VerificationTier {
    PUBLIC_PROFILE,  // 시스템이 선등록. 본인 계정 연결 전
    UNVERIFIED,      // 본인이 계정을 연결했으나 아직 미인증
    VERIFIED_USER,   // 이메일/실명 인증 완료
    VERIFIED_PRO     // 학력/활동 증빙 + 관리자 승인 완료
}
