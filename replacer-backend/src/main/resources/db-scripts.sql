DROP TABLE IF EXISTS replacement2;

CREATE TABLE replacement2 (
    id INT NOT NULL AUTO_INCREMENT,
    article_id INT NOT NULL,
    type VARCHAR(25) NOT NULL,
    subtype VARCHAR(30) COLLATE utf8mb4_bin NOT NULL,
    position INT NOT NULL DEFAULT 0,
    context VARCHAR(255) COLLATE utf8mb4_bin,
    last_update DATE NOT NULL,
    reviewer VARCHAR(100), -- In order to make the index work
    PRIMARY KEY (id),
    INDEX (article_id)
);

CREATE INDEX idx_count ON replacement2 (type, subtype, reviewer);
