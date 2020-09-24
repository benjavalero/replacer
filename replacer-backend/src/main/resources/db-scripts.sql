DROP TABLE IF EXISTS replacement2;

CREATE TABLE replacement2 (
    id INT NOT NULL AUTO_INCREMENT,
    article_id INT NOT NULL,
    lang VARCHAR(2),
    type VARCHAR(25) NOT NULL,
    subtype VARCHAR(30) COLLATE utf8mb4_bin NOT NULL,
    position INT NOT NULL DEFAULT 0,
    context VARCHAR(255) COLLATE utf8mb4_bin,
    last_update DATE NOT NULL,
    reviewer VARCHAR(100), -- In order to make the index work
    title VARCHAR(255) COLLATE utf8mb4_bin, -- For the sake of simplicity even if it breaks schema normality
    PRIMARY KEY (id)
);

-- To find random pages and count the group replacements
CREATE INDEX idx_count ON replacement2 (lang, reviewer, type, subtype);

-- Statistics
CREATE INDEX idx_reviewer ON replacement2 (reviewer);

-- Dump index
CREATE INDEX idx_dump ON replacement2 (lang, article_id, reviewer);
