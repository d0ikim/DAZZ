package com.dazz.backend.infrastructure.persistence.album;

import com.dazz.backend.application.port.out.AlbumRepository;
import com.dazz.backend.domain.album.Album;
import com.dazz.backend.domain.album.AlbumParticipation;
import com.dazz.backend.domain.album.ParticipationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AlbumRepositoryImpl implements AlbumRepository {

    private final AlbumJpaRepository albumJpaRepository;
    private final AlbumParticipationJpaRepository participationJpaRepository;

    @Override
    public Album save(Album album) {
        return toDomain(albumJpaRepository.save(toJpaEntity(album)));
    }

    @Override
    public Optional<Album> findById(Long id) {
        return albumJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public AlbumParticipation saveParticipation(AlbumParticipation participation) {
        return toDomain(participationJpaRepository.save(toJpaEntity(participation)));
    }

    @Override
    public List<AlbumParticipation> findParticipationsByMusicianId(Long musicianId) {
        return participationJpaRepository.findByMusicianId(musicianId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<AlbumParticipation> findParticipationsByMusicianIdAndType(Long musicianId, ParticipationType type) {
        return participationJpaRepository.findByMusicianIdAndParticipationType(musicianId, type).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public boolean existsParticipation(Long albumId, Long musicianId, ParticipationType type) {
        return participationJpaRepository.existsByAlbumIdAndMusicianIdAndParticipationType(albumId, musicianId, type);
    }

    private Album toDomain(AlbumJpaEntity entity) {
        return Album.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .releaseDate(entity.getReleaseDate())
                .coverImageUrl(entity.getCoverImageUrl())
                .albumReview(entity.getAlbumReview())
                .build();
    }

    private AlbumJpaEntity toJpaEntity(Album album) {
        return AlbumJpaEntity.builder()
                .id(album.getId())
                .title(album.getTitle())
                .releaseDate(album.getReleaseDate())
                .coverImageUrl(album.getCoverImageUrl())
                .albumReview(album.getAlbumReview())
                .build();
    }

    private AlbumParticipation toDomain(AlbumParticipationJpaEntity entity) {
        return AlbumParticipation.builder()
                .id(entity.getId())
                .albumId(entity.getAlbumId())
                .musicianId(entity.getMusicianId())
                .participationType(entity.getParticipationType())
                .build();
    }

    private AlbumParticipationJpaEntity toJpaEntity(AlbumParticipation participation) {
        return AlbumParticipationJpaEntity.builder()
                .id(participation.getId())
                .albumId(participation.getAlbumId())
                .musicianId(participation.getMusicianId())
                .participationType(participation.getParticipationType())
                .build();
    }
}
