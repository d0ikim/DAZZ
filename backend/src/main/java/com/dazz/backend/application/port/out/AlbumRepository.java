package com.dazz.backend.application.port.out;

import com.dazz.backend.domain.album.Album;
import com.dazz.backend.domain.album.AlbumParticipation;
import com.dazz.backend.domain.album.ParticipationType;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository {

    Album save(Album album);

    Optional<Album> findById(Long id);

    AlbumParticipation saveParticipation(AlbumParticipation participation);

    List<AlbumParticipation> findParticipationsByMusicianId(Long musicianId);

    List<AlbumParticipation> findParticipationsByMusicianIdAndType(Long musicianId, ParticipationType type);

    boolean existsParticipation(Long albumId, Long musicianId, ParticipationType type);
}
