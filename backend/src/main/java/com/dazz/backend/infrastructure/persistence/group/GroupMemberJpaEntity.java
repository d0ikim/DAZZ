package com.dazz.backend.infrastructure.persistence.group;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GroupMemberJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "musician_id", nullable = false)
    private Long musicianId;

    @Column(length = 50)
    private String role;
}
