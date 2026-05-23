package com.dazz.backend.infrastructure.persistence.group;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GroupJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "genre_tags", length = 255)
    private String genreTags;

    @Column(columnDefinition = "TEXT")
    private String description;
}
