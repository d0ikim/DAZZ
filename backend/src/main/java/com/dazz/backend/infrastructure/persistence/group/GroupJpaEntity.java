package com.dazz.backend.infrastructure.persistence.group;

import com.dazz.backend.infrastructure.persistence.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @Convert(converter = StringListConverter.class)
    @Column(name = "genre_tags", length = 255)
    private List<String> genreTags;

    @Column(columnDefinition = "TEXT")
    private String description;
}
