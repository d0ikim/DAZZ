package com.dazz.backend.infrastructure.persistence.group;

import com.dazz.backend.application.port.out.GroupRepository;
import com.dazz.backend.domain.group.Group;
import com.dazz.backend.domain.group.GroupMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepository {

    private final GroupJpaRepository groupJpaRepository;
    private final GroupMemberJpaRepository memberJpaRepository;

    @Override
    public Group save(Group group) {
        return toDomain(groupJpaRepository.save(toJpaEntity(group)));
    }

    @Override
    public Optional<Group> findById(Long id) {
        return groupJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public GroupMember saveMember(GroupMember member) {
        return toDomain(memberJpaRepository.save(toJpaEntity(member)));
    }

    @Override
    public List<GroupMember> findMembersByGroupId(Long groupId) {
        return memberJpaRepository.findByGroupId(groupId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Group> findGroupsByMusicianId(Long musicianId) {
        List<Long> groupIds = memberJpaRepository.findByMusicianId(musicianId).stream()
                .map(GroupMemberJpaEntity::getGroupId)
                .toList();
        if (groupIds.isEmpty()) {
            return List.of();
        }
        return groupJpaRepository.findAllById(groupIds).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsMember(Long groupId, Long musicianId) {
        return memberJpaRepository.existsByGroupIdAndMusicianId(groupId, musicianId);
    }

    private Group toDomain(GroupJpaEntity entity) {
        return Group.builder()
                .id(entity.getId())
                .groupName(entity.getGroupName())
                .genreTags(entity.getGenreTags())
                .description(entity.getDescription())
                .build();
    }

    private GroupJpaEntity toJpaEntity(Group group) {
        return GroupJpaEntity.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .genreTags(group.getGenreTags())
                .description(group.getDescription())
                .build();
    }

    private GroupMember toDomain(GroupMemberJpaEntity entity) {
        return GroupMember.builder()
                .id(entity.getId())
                .groupId(entity.getGroupId())
                .musicianId(entity.getMusicianId())
                .role(entity.getRole())
                .build();
    }

    private GroupMemberJpaEntity toJpaEntity(GroupMember member) {
        return GroupMemberJpaEntity.builder()
                .id(member.getId())
                .groupId(member.getGroupId())
                .musicianId(member.getMusicianId())
                .role(member.getRole())
                .build();
    }
}
