CREATE TABLE album
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    release_date     DATE         NULL,
    cover_image_url  VARCHAR(500) NULL,
    album_review     TEXT         NULL
);

CREATE TABLE album_participation
(
    id                   BIGINT      AUTO_INCREMENT PRIMARY KEY,
    album_id             BIGINT      NOT NULL,
    musician_id          BIGINT      NOT NULL,
    participation_type   VARCHAR(30) NOT NULL,
    UNIQUE KEY uq_album_participation (album_id, musician_id, participation_type),
    CONSTRAINT fk_ap_album    FOREIGN KEY (album_id)    REFERENCES album (id),
    CONSTRAINT fk_ap_musician FOREIGN KEY (musician_id) REFERENCES musician (id)
);

CREATE INDEX idx_participation_musician ON album_participation (musician_id, participation_type);
