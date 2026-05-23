package com.dazz.backend.infrastructure.persistence.musician;

import com.dazz.backend.domain.musician.RelationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollaborationJpaRepository extends JpaRepository<CollaborationJpaEntity, Long> {

    Optional<CollaborationJpaEntity> findByFromMusicianIdAndToMusicianIdAndRelationType(
            Long fromMusicianId, Long toMusicianId, RelationType relationType);

    @Query("SELECT c FROM CollaborationJpaEntity c " +
           "WHERE c.fromMusicianId = :musicianId OR c.toMusicianId = :musicianId")
    List<CollaborationJpaEntity> findByMusicianId(@Param("musicianId") Long musicianId);
}
