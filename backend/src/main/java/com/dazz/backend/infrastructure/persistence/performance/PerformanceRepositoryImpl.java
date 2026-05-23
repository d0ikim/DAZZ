package com.dazz.backend.infrastructure.persistence.performance;

import com.dazz.backend.application.port.out.ClubRepository;
import com.dazz.backend.application.port.out.PerformanceRepository;
import com.dazz.backend.domain.performance.Club;
import com.dazz.backend.domain.performance.Performance;
import com.dazz.backend.domain.performance.PerformanceLineup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PerformanceRepositoryImpl implements PerformanceRepository, ClubRepository {

    private final ClubJpaRepository clubJpaRepository;
    private final PerformanceJpaRepository performanceJpaRepository;
    private final PerformanceLineupJpaRepository lineupJpaRepository;

    // --- ClubRepository ---

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

    // --- PerformanceRepository ---

    @Override
    public Performance save(Performance performance) {
        return toDomain(performanceJpaRepository.save(toJpaEntity(performance)));
    }

    @Override
    public Optional<Performance> findById(Long id) {
        return performanceJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Performance> findByClubId(Long clubId) {
        return performanceJpaRepository.findByClubId(clubId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public PerformanceLineup saveLineup(PerformanceLineup lineup) {
        return toDomain(lineupJpaRepository.save(toJpaEntity(lineup)));
    }

    @Override
    public List<PerformanceLineup> findLineupByPerformanceId(Long performanceId) {
        return lineupJpaRepository.findByPerformanceId(performanceId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<PerformanceLineup> findLineupByMusicianId(Long musicianId) {
        return lineupJpaRepository.findByMusicianId(musicianId).stream()
                .map(this::toDomain).toList();
    }

    // --- Mappers ---

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

    private Performance toDomain(PerformanceJpaEntity entity) {
        return Performance.builder()
                .id(entity.getId())
                .clubId(entity.getClubId())
                .startTime(entity.getStartTime())
                .title(entity.getTitle())
                .genre(entity.getGenre())
                .setList(entity.getSetList())
                .build();
    }

    private PerformanceJpaEntity toJpaEntity(Performance performance) {
        return PerformanceJpaEntity.builder()
                .id(performance.getId())
                .clubId(performance.getClubId())
                .startTime(performance.getStartTime())
                .title(performance.getTitle())
                .genre(performance.getGenre())
                .setList(performance.getSetList())
                .build();
    }

    private PerformanceLineup toDomain(PerformanceLineupJpaEntity entity) {
        return PerformanceLineup.builder()
                .id(entity.getId())
                .performanceId(entity.getPerformanceId())
                .musicianId(entity.getMusicianId())
                .setInfo(entity.getSetInfo())
                .build();
    }

    private PerformanceLineupJpaEntity toJpaEntity(PerformanceLineup lineup) {
        return PerformanceLineupJpaEntity.builder()
                .id(lineup.getId())
                .performanceId(lineup.getPerformanceId())
                .musicianId(lineup.getMusicianId())
                .setInfo(lineup.getSetInfo())
                .build();
    }
}
