package com.dazz.backend.infrastructure.persistence.musician;

import com.dazz.backend.application.port.out.MusicianRepository;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.infrastructure.persistence.musician.mapper.MusicianPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MusicianRepositoryImpl implements MusicianRepository {

    private final MusicianJpaRepository jpaRepository;

    @Override
    public Musician save(Musician musician) {
        return MusicianPersistenceMapper.toDomain(
                jpaRepository.save(MusicianPersistenceMapper.toJpaEntity(musician)));
    }

    @Override
    public Optional<Musician> findById(Long id) {
        return jpaRepository.findById(id).map(MusicianPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Musician> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid.toString()).map(MusicianPersistenceMapper::toDomain);
    }

    @Override
    public List<Musician> findAll() {
        return jpaRepository.findAll().stream()
                .map(MusicianPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Musician> findAllByIds(List<Long> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(MusicianPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaRepository.existsByUserId(userId);
    }
}
