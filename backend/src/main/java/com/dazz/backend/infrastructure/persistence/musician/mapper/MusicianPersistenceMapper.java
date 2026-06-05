package com.dazz.backend.infrastructure.persistence.musician.mapper;

import com.dazz.backend.domain.musician.Collaboration;
import com.dazz.backend.domain.musician.Musician;
import com.dazz.backend.infrastructure.persistence.musician.CollaborationJpaEntity;
import com.dazz.backend.infrastructure.persistence.musician.MusicianJpaEntity;

import java.util.UUID;

public class MusicianPersistenceMapper {

    private MusicianPersistenceMapper() {}

    public static Musician toDomain(MusicianJpaEntity entity) {
        return Musician.builder()
                .id(entity.getId())
                .uuid(UUID.fromString(entity.getUuid()))
                .userId(entity.getUserId())
                .stageName(entity.getStageName())
                .realName(entity.getRealName())
                .position(entity.getPosition())
                .bio(entity.getBio())
                .snsUrl(entity.getSnsUrl())
                .profileImageUrl(entity.getProfileImageUrl())
                .verificationTier(entity.getVerificationTier())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static MusicianJpaEntity toJpaEntity(Musician musician) {
        return MusicianJpaEntity.builder()
                .id(musician.getId())
                .uuid(musician.getUuid().toString())
                .userId(musician.getUserId())
                .stageName(musician.getStageName())
                .realName(musician.getRealName())
                .position(musician.getPosition())
                .bio(musician.getBio())
                .snsUrl(musician.getSnsUrl())
                .profileImageUrl(musician.getProfileImageUrl())
                .verificationTier(musician.getVerificationTier())
                .createdAt(musician.getCreatedAt())
                .build();
    }

    public static Collaboration toDomain(CollaborationJpaEntity entity) {
        return Collaboration.builder()
                .id(entity.getId())
                .fromMusicianId(entity.getFromMusicianId())
                .toMusicianId(entity.getToMusicianId())
                .relationType(entity.getRelationType())
                .weight(entity.getWeight())
                .build();
    }

    public static CollaborationJpaEntity toJpaEntity(Collaboration collaboration) {
        return CollaborationJpaEntity.builder()
                .id(collaboration.getId())
                .fromMusicianId(collaboration.getFromMusicianId())
                .toMusicianId(collaboration.getToMusicianId())
                .relationType(collaboration.getRelationType())
                .weight(collaboration.getWeight())
                .build();
    }
}
