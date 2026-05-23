package com.dazz.backend.infrastructure.persistence.musician;

import com.dazz.backend.domain.musician.RelationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "collaboration")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CollaborationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_musician_id", nullable = false)
    private Long fromMusicianId;

    @Column(name = "to_musician_id", nullable = false)
    private Long toMusicianId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 30)
    private RelationType relationType;

    @Column(nullable = false)
    private int weight;
}
