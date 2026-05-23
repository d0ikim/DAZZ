package com.dazz.backend.application.musician;

import com.dazz.backend.application.port.out.MusicianRepository;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.domain.musician.exception.MusicianNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MusicianQueryService {

    private final MusicianRepository musicianRepository;

    public Musician getByUuid(UUID uuid) {
        return musicianRepository.findByUuid(uuid)
                .orElseThrow(() -> new MusicianNotFoundException(uuid));
    }

    public List<Musician> getAll() {
        return musicianRepository.findAll();
    }
}
