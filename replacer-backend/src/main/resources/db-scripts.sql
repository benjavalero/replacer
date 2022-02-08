CREATE TABLE page (
    lang CHAR(2) CHARACTER SET ascii NOT NULL,
    article_id MEDIUMINT UNSIGNED NOT NULL,
    last_update DATE NOT NULL,
    title VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
    PRIMARY KEY (lang, article_id)
);

CREATE TABLE replacement (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    lang CHAR(2) CHARACTER SET ascii NOT NULL,
    article_id MEDIUMINT UNSIGNED NOT NULL,
    type TINYINT NOT NULL,
    subtype VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    position MEDIUMINT UNSIGNED NOT NULL DEFAULT 0,
    context VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
    reviewer VARCHAR(100), -- In order to make the index work
    PRIMARY KEY (id)
);

ALTER TABLE replacement
ADD CONSTRAINT fk_page_id FOREIGN KEY (lang, article_id) REFERENCES page (lang, article_id) ON DELETE CASCADE;

-- To find random pages and count the group replacements
CREATE INDEX idx_count ON replacement (lang, reviewer, type, subtype);
CREATE INDEX idx_count_no_type ON replacement (lang, reviewer);

-- Statistics
CREATE INDEX idx_reviewer ON replacement (reviewer);

-- Dump index
CREATE INDEX idx_dump ON replacement (lang, article_id, reviewer);

-- New table only for custom replacements
CREATE TABLE custom (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    lang CHAR(2) CHARACTER SET ascii NOT NULL,
    article_id MEDIUMINT UNSIGNED NOT NULL,
    replacement VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    cs TINYINT(1) NOT NULL DEFAULT 0,
    position MEDIUMINT UNSIGNED NOT NULL DEFAULT 0, -- So we can move items to the replacements table
    context VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
    reviewer VARCHAR(100), -- In order to make the index work
    PRIMARY KEY (id)
);

ALTER TABLE custom
ADD CONSTRAINT fk_custom_page_id FOREIGN KEY (lang, article_id) REFERENCES page (lang, article_id) ON DELETE CASCADE;
