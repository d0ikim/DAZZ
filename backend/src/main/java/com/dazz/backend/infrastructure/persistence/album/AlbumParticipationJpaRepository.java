package com.dazz.backend.infrastructure.persistence.album;

import com.dazz.backend.domain.album.ParticipationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumParticipationJpaRepository extends JpaRepository<AlbumParticipationJpaEntity, Long> {

    List<AlbumParticipationJpaEntity> findByMusicianId(Long musicianId);

    List<AlbumParticipationJpaEntity> findByMusicianIdAndParticipationType(
            Long musicianId, ParticipationType participationType);

    boolean existsByAlbumIdAndMusicianIdAndParticipationType(
            Long albumId, Long musicianId, ParticipationType participationType);
}
