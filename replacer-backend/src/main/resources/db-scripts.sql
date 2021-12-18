DROP TABLE IF EXISTS replacement;

CREATE TABLE replacement (
    id INT NOT NULL AUTO_INCREMENT,
    article_id INT NOT NULL,
    lang VARCHAR(2),
    type VARCHAR(25) NOT NULL,
    subtype VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    position INT NOT NULL DEFAULT 0,
    context VARCHAR(255) COLLATE utf8mb4_bin,
    last_update DATE NOT NULL,
    reviewer VARCHAR(100), -- In order to make the index work
    title VARCHAR(255) COLLATE utf8mb4_bin, -- For the sake of simplicity even if it breaks schema normality
    PRIMARY KEY (id)
);

-- To find random pages and count the group replacements
CREATE INDEX idx_count ON replacement (lang, reviewer, type, subtype);
CREATE INDEX idx_count_no_type ON replacement (lang, reviewer);

-- Statistics
CREATE INDEX idx_reviewer ON replacement (reviewer);

-- Dump index
CREATE INDEX idx_dump ON replacement (lang, article_id, reviewer);

-- New table only for custom replacements
CREATE TABLE custom (
    id INT NOT NULL AUTO_INCREMENT,
    article_id INT NOT NULL,
    lang VARCHAR(2) NOT NULL,
    replacement VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    cs TINYINT(1) NOT NULL DEFAULT 0,
    last_update DATE NOT NULL,
    reviewer VARCHAR(100) NOT NULL, -- In order to make the index work
    PRIMARY KEY (id)
);

-- New table only for custom replacements
CREATE TABLE page (
    lang VARCHAR(2) NOT NULL,
    article_id INT NOT NULL,
    title VARCHAR(255) COLLATE utf8mb4_bin,
    PRIMARY KEY (lang, article_id)
);
