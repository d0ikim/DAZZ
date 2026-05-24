package com.dazz.backend.infrastructure.persistence.club;

import com.dazz.backend.application.port.out.ClubRepository;
import com.dazz.backend.domain.club.Club;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClubRepositoryImpl implements ClubRepository {

    private final ClubJpaRepository clubJpaRepository;

    @Override
    public Club save(Club club) {
        return toDomain(clubJpaRepository.save(toJpaEntity(club)));
    }

    @Override
    public Optional<Club> findById(Long id) {
        return clubJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Club> findAll() {
        return clubJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private Club toDomain(ClubJpaEntity entity) {
        return Club.builder()
                .id(entity.getId())
                .name(entity.getName())
                .location(entity.getLocation())
                .instagramUrl(entity.getInstagramUrl())
                .build();
    }

    private ClubJpaEntity toJpaEntity(Club club) {
        return ClubJpaEntity.builder()
                .id(club.getId())
                .name(club.getName())
                .location(club.getLocation())
                .instagramUrl(club.getInstagramUrl())
                .build();
    }
}
