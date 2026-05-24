CREATE TABLE musician
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid              CHAR(36)     NOT NULL,
    user_id           BIGINT       NULL,
    stage_name        VARCHAR(100) NOT NULL,
    real_name         VARCHAR(100) NULL,
    position          VARCHAR(30)  NOT NULL,
    bio               TEXT         NULL,
    sns_url           VARCHAR(500) NULL,
    profile_image_url VARCHAR(500) NULL,
    verification_tier VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC_PROFILE',
    created_at        DATETIME     NOT NULL,
    UNIQUE KEY uq_musician_uuid (uuid),
    UNIQUE KEY uq_musician_user_id (user_id),
    CONSTRAINT fk_musician_user FOREIGN KEY (user_id) REFERENCES `user` (id)
);
