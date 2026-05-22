package com.dazz.backend.application.port.out;

import com.dazz.backend.domain.musician.Collaboration;
import com.dazz.backend.domain.musician.RelationType;

import java.util.List;
import java.util.Optional;

public interface CollaborationRepository {

    Collaboration save(Collaboration collaboration);

    Optional<Collaboration> findByFromAndToAndType(Long fromMusicianId, Long toMusicianId, RelationType relationType);

    List<Collaboration> findByMusicianId(Long musicianId);
}
