package com.dazz.backend.application.port.out;

import com.dazz.backend.domain.musician.Musician;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 뮤지션 영속성 포트. 실제 구현은 infrastructure/persistence에서 담당.
 */
public interface MusicianRepository {

    Musician save(Musician musician);

    Optional<Musician> findById(Long id);

    Optional<Musician> findByUuid(UUID uuid);

    List<Musician> findAll();

    boolean existsByUserId(Long userId);
}
