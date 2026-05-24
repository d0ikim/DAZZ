package com.dazz.backend.application.musician;

import com.dazz.backend.application.port.out.CollaborationRepository;
import com.dazz.backend.application.port.out.MusicianRepository;
import com.dazz.backend.domain.musician.Collaboration;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MusicianQueryService {

    private final MusicianRepository musicianRepository;
    private final CollaborationRepository collaborationRepository;

    public Musician getByUuid(UUID uuid) {
        return musicianRepository.findByUuid(uuid)
                .orElseThrow(() -> new MusicianNotFoundException(uuid));
    }

    public List<Musician> getAll() {
        return musicianRepository.findAll();
    }

    /**
     * 뮤지션 인사이트 조회: 프로필 + 협업 네트워크 (depth=1 직접 협업자).
     * depth=2는 MVP 이후 구현 예정 — 현재는 depth=1과 동일하게 동작.
     */
    public MusicianInsightResult getInsight(Long musicianId, boolean includeNetwork, int depth) {
        Musician musician = musicianRepository.findById(musicianId)
                .orElseThrow(() -> new MusicianNotFoundException(musicianId));

        if (!includeNetwork) {
            return new MusicianInsightResult(musician, List.of());
        }

        List<Collaboration> collaborations = collaborationRepository.findByMusicianId(musicianId);

        List<Long> collaboratorIds = collaborations.stream()
                .map(c -> c.getFromMusicianId().equals(musicianId) ? c.getToMusicianId() : c.getFromMusicianId())
                .distinct()
                .toList();

        Map<Long, Musician> collaboratorMap = musicianRepository.findAllByIds(collaboratorIds).stream()
                .collect(Collectors.toMap(Musician::getId, m -> m));

        List<MusicianInsightResult.NetworkEntry> network = collaborations.stream()
                .map(c -> {
                    Long collaboratorId = c.getFromMusicianId().equals(musicianId)
                            ? c.getToMusicianId()
                            : c.getFromMusicianId();
                    Musician collaborator = collaboratorMap.get(collaboratorId);
                    if (collaborator == null) return null;
                    return new MusicianInsightResult.NetworkEntry(collaborator, c.getRelationType(), c.getWeight());
                })
                .filter(Objects::nonNull)
                .toList();

        return new MusicianInsightResult(musician, network);
    }
}

