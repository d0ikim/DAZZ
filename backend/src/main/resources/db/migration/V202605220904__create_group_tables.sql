CREATE TABLE `group`
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_name  VARCHAR(100) NOT NULL,
    genre_tags  VARCHAR(255) NULL,
    description TEXT         NULL
);

CREATE TABLE group_member
(
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    group_id    BIGINT      NOT NULL,
    musician_id BIGINT      NOT NULL,
    role        VARCHAR(50) NULL,
    UNIQUE KEY uq_group_member (group_id, musician_id),
    CONSTRAINT fk_gm_group    FOREIGN KEY (group_id)    REFERENCES `group` (id),
    CONSTRAINT fk_gm_musician FOREIGN KEY (musician_id) REFERENCES musician (id)
);
