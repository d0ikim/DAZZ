package com.dazz.backend.application.musician;

import com.dazz.backend.application.port.out.MusicianRepository;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 관리자 뮤지션 승인/거절 서비스.
 * EC-01 정책: 관리자가 수동으로 승인/거절한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminMusicianService {

    private final MusicianRepository musicianRepository;

    /**
     * UNVERIFIED 뮤지션을 VERIFIED_USER로 승인.
     * 이미 VERIFIED_USER 또는 VERIFIED_PRO이면 MusicianNotApprovableException 발생.
     */
    public Musician approve(UUID uuid) {
        Musician musician = musicianRepository.findByUuid(uuid)
                .orElseThrow(() -> new MusicianNotFoundException(uuid));

        // 도메인 메서드에서 상태 불변 조건 검증
        Musician approved = musician.approve();
        return musicianRepository.save(approved);
    }

    /**
     * UNVERIFIED 뮤지션을 PUBLIC_PROFILE로 거절 + userId null 초기화.
     * PUBLIC_PROFILE 상태(claim 안 된 뮤지션)에 거절 시도하면 MusicianNotRejectableException 발생.
     */
    public Musician reject(UUID uuid) {
        Musician musician = musicianRepository.findByUuid(uuid)
                .orElseThrow(() -> new MusicianNotFoundException(uuid));

        // 도메인 메서드에서 상태 불변 조건 검증
        Musician rejected = musician.reject();
        return musicianRepository.save(rejected);
    }
}