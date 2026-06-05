package com.dazz.backend.application.musician;

import com.dazz.backend.application.musician.command.MusicianRegisterCommand;
import com.dazz.backend.application.port.out.MusicianRepository;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.exception.MusicianAlreadyClaimedException;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import com.dazz.backend.domain.musician.exception.MusicianUserAlreadyLinkedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MusicianCommandService {

    private final MusicianRepository musicianRepository;

    public Musician register(MusicianRegisterCommand command) {
        Musician musician = Musician.register(
                command.stageName(),
                command.realName(),
                command.position(),
                command.bio(),
                command.snsUrl(),
                command.profileImageUrl()
        );
        return musicianRepository.save(musician);
    }

    /**
     * 사용자가 본인의 계정을 Public Profile에 연결.
     * EC-01: 한 User는 최대 하나의 Musician 프로필만 가질 수 있다.
     */
    public Musician claim(UUID uuid, Long userId) {
        Musician musician = musicianRepository.findByUuid(uuid)
                .orElseThrow(() -> new MusicianNotFoundException(uuid));

        if (musician.isClaimed()) {
            throw new MusicianAlreadyClaimedException(uuid);
        }
        if (musicianRepository.existsByUserId(userId)) {
            throw new MusicianUserAlreadyLinkedException(userId);
        }

        return musicianRepository.save(musician.claim(userId));
    }
}
