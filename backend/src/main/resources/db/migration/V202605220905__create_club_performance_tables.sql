CREATE TABLE club
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    location      VARCHAR(255) NULL,
    instagram_url VARCHAR(500) NULL
);

CREATE TABLE performance
(
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    club_id    BIGINT       NOT NULL,
    start_time DATETIME     NOT NULL,
    title      VARCHAR(255) NOT NULL,
    genre      VARCHAR(50)  NULL,
    set_list   TEXT         NULL,
    CONSTRAINT fk_performance_club FOREIGN KEY (club_id) REFERENCES club (id)
);

CREATE INDEX idx_performance_club_time ON performance (club_id, start_time);

CREATE TABLE performance_lineup
(
    id             BIGINT      AUTO_INCREMENT PRIMARY KEY,
    performance_id BIGINT      NOT NULL,
    musician_id    BIGINT      NOT NULL,
    set_info       VARCHAR(100) NULL,
    UNIQUE KEY uq_lineup (performance_id, musician_id),
    CONSTRAINT fk_lineup_performance FOREIGN KEY (performance_id) REFERENCES performance (id),
    CONSTRAINT fk_lineup_musician    FOREIGN KEY (musician_id)    REFERENCES musician (id)
);