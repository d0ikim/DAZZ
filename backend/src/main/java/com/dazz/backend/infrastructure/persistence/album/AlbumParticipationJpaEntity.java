package com.dazz.backend.infrastructure.persistence.album;

import com.dazz.backend.domain.album.ParticipationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "album_participation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AlbumParticipationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "album_id", nullable = false)
    private Long albumId;

    @Column(name = "musician_id", nullable = false)
    private Long musicianId;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_type", nullable = false, length = 30)
    private ParticipationType participationType;
}
