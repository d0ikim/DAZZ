package com.dazz.backend.application.port.out;

import com.dazz.backend.domain.club.Club;

import java.util.List;
import java.util.Optional;

public interface ClubRepository {

    Club save(Club club);

    Optional<Club> findById(Long id);

    List<Club> findAll();
}
