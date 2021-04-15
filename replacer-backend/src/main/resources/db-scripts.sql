DROP TABLE IF EXISTS replacement2;

CREATE TABLE replacement2 (
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
CREATE INDEX idx_count ON replacement2 (lang, reviewer, type, subtype);
CREATE INDEX idx_count_no_type ON replacement2 (lang, reviewer);

-- Statistics
CREATE INDEX idx_reviewer ON replacement2 (reviewer);

-- Dump index
CREATE INDEX idx_dump ON replacement2 (lang, article_id, reviewer);

-- Rename replacement table
RENAME TABLE replacement2 TO replacement;

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

-- Move from replacement to custom
INSERT INTO custom(article_id, lang, replacement, last_update, reviewer)
    SELECT article_id, lang, subtype, last_update, reviewer
    FROM replacement WHERE type = 'Personalizado';
DELETE FROM replacement WHERE type = 'Personalizado';

-- New table only for custom replacements
CREATE TABLE page (
    lang VARCHAR(2) NOT NULL,
    article_id INT NOT NULL,
    title VARCHAR(255) COLLATE utf8mb4_bin,
    PRIMARY KEY (lang, article_id)
);
-- For the moment we don't need to define FK between Replacement/Custom and Page at DB level
-- Move titles to page table
INSERT IGNORE INTO page(lang, article_id, title)
    SELECT lang, article_id, title
    FROM replacement WHERE title IS NOT NULL;
ALTER TABLE replacement DROP COLUMN title;
