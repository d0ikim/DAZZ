package com.dazz.backend.infrastructure.persistence.album;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumJpaRepository extends JpaRepository<AlbumJpaEntity, Long> {
}
