package com.dazz.backend.infrastructure.persistence.musician;

import com.dazz.backend.domain.musician.Position;
import com.dazz.backend.domain.musician.VerificationTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "musician")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MusicianJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "CHAR(36)")
    private String uuid;

    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(name = "stage_name", nullable = false, length = 100)
    private String stageName;

    @Column(name = "real_name", length = 100)
    private String realName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Position position;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "sns_url", length = 500)
    private String snsUrl;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_tier", nullable = false, length = 20)
    private VerificationTier verificationTier;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
