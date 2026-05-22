CREATE TABLE collaboration
(
    id                 BIGINT      AUTO_INCREMENT PRIMARY KEY,
    from_musician_id   BIGINT      NOT NULL,
    to_musician_id     BIGINT      NOT NULL,
    relation_type      VARCHAR(30) NOT NULL,
    weight             INT         NOT NULL DEFAULT 1,
    UNIQUE KEY uq_collaboration (from_musician_id, to_musician_id, relation_type),
    CONSTRAINT fk_collaboration_from FOREIGN KEY (from_musician_id) REFERENCES musician (id),
    CONSTRAINT fk_collaboration_to   FOREIGN KEY (to_musician_id)   REFERENCES musician (id)
);

CREATE INDEX idx_collaboration_weight ON collaboration (from_musician_id, weight DESC);
