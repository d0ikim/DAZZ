package com.dazz.backend.domain.musician;

/**
 * 뮤지션 간 협업 관계 유형. ERD의 COLLABORATION.relation_type 컬럼에 대응.
 */
public enum RelationType {
    COLLABORATION,  // 일반 협업 (세션, 공연 등)
    MENTOR,         // 스승-제자 관계
    BAND_MEMBER     // 동일 밴드/그룹 소속
}