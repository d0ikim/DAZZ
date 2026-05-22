package com.dazz.backend.application.port.out;

import com.dazz.backend.domain.group.Group;
import com.dazz.backend.domain.group.GroupMember;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {

    Group save(Group group);

    Optional<Group> findById(Long id);

    GroupMember saveMember(GroupMember groupMember);

    List<GroupMember> findMembersByGroupId(Long groupId);

    List<GroupMember> findGroupsByMusicianId(Long musicianId);

    boolean existsMember(Long groupId, Long musicianId);
}
