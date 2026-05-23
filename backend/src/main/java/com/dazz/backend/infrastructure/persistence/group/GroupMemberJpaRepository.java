package com.dazz.backend.infrastructure.persistence.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMemberJpaRepository extends JpaRepository<GroupMemberJpaEntity, Long> {

    List<GroupMemberJpaEntity> findByGroupId(Long groupId);

    List<GroupMemberJpaEntity> findByMusicianId(Long musicianId);

    boolean existsByGroupIdAndMusicianId(Long groupId, Long musicianId);
}
