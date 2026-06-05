package com.dazz.backend.application.collaboration;

import com.dazz.backend.application.collaboration.command.CollaborationLinkCommand;
import com.dazz.backend.application.port.out.CollaborationRepository;
import com.dazz.backend.application.port.out.MusicianRepository;
import com.dazz.backend.domain.musician.Collaboration;
import com.dazz.backend.domain.musician.exception.CollaborationSelfReferenceException;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollaborationCommandService {

    private final CollaborationRepository collaborationRepository;
    private final MusicianRepository musicianRepository;

    /**
     * 협업 관계 등록 또는 가중치 증가.
     * - 신규: weight=1로 INSERT
     * - 기존: weight+1로 UPDATE
     * from/to 정규화(min < max)로 양방향 중복을 방지한다.
     */
    @Transactional
    public CollaborationResult linkOrIncrement(CollaborationLinkCommand command) {
        if (command.fromMusicianId().equals(command.toMusicianId())) {
            throw new CollaborationSelfReferenceException(command.fromMusicianId());
        }
        if (musicianRepository.findById(command.fromMusicianId()).isEmpty()) {
            throw new MusicianNotFoundException(command.fromMusicianId());
        }
        if (musicianRepository.findById(command.toMusicianId()).isEmpty()) {
            throw new MusicianNotFoundException(command.toMusicianId());
        }

        long minId = Math.min(command.fromMusicianId(), command.toMusicianId());
        long maxId = Math.max(command.fromMusicianId(), command.toMusicianId());

        Optional<Collaboration> existing =
                collaborationRepository.findByFromAndToAndType(minId, maxId, command.relationType());

        if (existing.isPresent()) {
            Collaboration updated = collaborationRepository.save(existing.get().incrementWeight());
            return CollaborationResult.of(updated, false);
        }

        Collaboration created = collaborationRepository.save(
                Collaboration.newPair(minId, maxId, command.relationType()));
        return CollaborationResult.of(created, true);
    }
}