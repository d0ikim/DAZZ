package com.dazz.backend.infrastructure.persistence.musician;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MusicianJpaRepository extends JpaRepository<MusicianJpaEntity, Long> {

    Optional<MusicianJpaEntity> findByUuid(String uuid);

    boolean existsByUserId(Long userId);
}
