package com.dazz.backend.infrastructure.persistence.musician;

import com.dazz.backend.application.port.out.CollaborationRepository;
import com.dazz.backend.domain.musician.Collaboration;
import com.dazz.backend.domain.musician.RelationType;
import com.dazz.backend.infrastructure.persistence.musician.mapper.MusicianPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CollaborationRepositoryImpl implements CollaborationRepository {

    private final CollaborationJpaRepository jpaRepository;

    @Override
    public Collaboration save(Collaboration collaboration) {
        return MusicianPersistenceMapper.toDomain(
                jpaRepository.save(MusicianPersistenceMapper.toJpaEntity(collaboration)));
    }

    @Override
    public Optional<Collaboration> findByFromAndToAndType(Long fromId, Long toId, RelationType type) {
        return jpaRepository.findByFromMusicianIdAndToMusicianIdAndRelationType(fromId, toId, type)
                .map(MusicianPersistenceMapper::toDomain);
    }

    @Override
    public List<Collaboration> findByMusicianId(Long musicianId) {
        return jpaRepository.findByMusicianId(musicianId).stream()
                .map(MusicianPersistenceMapper::toDomain)
                .toList();
    }
}
