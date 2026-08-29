CREATE TABLE IF NOT EXISTS lang (
    code CHAR(2) CHARACTER SET ascii NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (code)
);

INSERT INTO lang (code, name) VALUES ('es', 'SPANISH');
INSERT INTO lang (code, name) VALUES ('gl', 'GALICIAN');

CREATE TABLE page (
    lang CHAR(2) CHARACTER SET ascii NOT NULL,
    page_id MEDIUMINT UNSIGNED NOT NULL,
    last_update DATE NOT NULL,
    title VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
    PRIMARY KEY (lang, page_id)
);

-- FK page --> lang
ALTER TABLE page
ADD CONSTRAINT fk_page_lang FOREIGN KEY (lang) REFERENCES lang (code);

CREATE TABLE IF NOT EXISTS replacement_kind (
    code TINYINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (code)
);

INSERT INTO replacement_kind (code, name) VALUES (2, 'SIMPLE');
INSERT INTO replacement_kind (code, name) VALUES (3, 'COMPOSED');
INSERT INTO replacement_kind (code, name) VALUES (5, 'STYLE');

CREATE TABLE IF NOT EXISTS review_type (
    code TINYINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (code)
);

INSERT INTO review_type (code, name) VALUES (0, 'UNKNOWN');
INSERT INTO review_type (code, name) VALUES (1, 'MODIFIED');
INSERT INTO review_type (code, name) VALUES (2, 'NOT MODIFIED');
INSERT INTO review_type (code, name) VALUES (3, 'IGNORED');

-- Replacements pending review (no review columns needed)
CREATE TABLE replacement (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    lang CHAR(2) CHARACTER SET ascii NOT NULL,
    page_id MEDIUMINT UNSIGNED NOT NULL,
    kind TINYINT NOT NULL,
    subtype VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    start MEDIUMINT UNSIGNED NOT NULL,
    context VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
    PRIMARY KEY (id)
);

-- FK replacement --> page
ALTER TABLE replacement
ADD CONSTRAINT fk_page_id FOREIGN KEY (lang, page_id) REFERENCES page (lang, page_id) ON DELETE CASCADE;

-- FK replacement --> kind
ALTER TABLE replacement
ADD CONSTRAINT fk_replacement_kind FOREIGN KEY (kind) REFERENCES replacement_kind (code);

-- Find and count pages to review by subtype (or no type)
CREATE INDEX idx_replacement_type ON replacement (lang, kind, subtype, page_id);

-- Find not-reviewed replacements by id (for random page without type filter)
CREATE INDEX idx_replacement_id ON replacement (lang, id);

-- Group not-reviewed replacements by page (for top pages with most replacements)
CREATE INDEX idx_replacement_page ON replacement (lang, page_id);

-- Reviewed replacements
CREATE TABLE reviewed (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    lang CHAR(2) CHARACTER SET ascii NOT NULL,
    page_id MEDIUMINT UNSIGNED NOT NULL,
    kind TINYINT NOT NULL,
    subtype VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    start MEDIUMINT UNSIGNED NOT NULL,
    context VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
    reviewer VARCHAR(40) NOT NULL,
    review_type TINYINT NOT NULL,
    review_timestamp TIMESTAMP NOT NULL,
    old_rev_id INTEGER UNSIGNED,
    new_rev_id INTEGER UNSIGNED,
    PRIMARY KEY (id)
);

-- FK reviewed --> page
ALTER TABLE reviewed
ADD CONSTRAINT fk_reviewed_page_id FOREIGN KEY (lang, page_id) REFERENCES page (lang, page_id) ON DELETE CASCADE;

-- FK reviewed --> kind
ALTER TABLE reviewed
ADD CONSTRAINT fk_reviewed_kind FOREIGN KEY (kind) REFERENCES replacement_kind (code);

-- FK reviewed --> review type
ALTER TABLE reviewed
ADD CONSTRAINT fk_reviewed_review_type FOREIGN KEY (review_type) REFERENCES review_type (code);

-- Count reviewed grouped by reviewer
CREATE INDEX idx_reviewed_reviewer ON reviewed (lang, reviewer);

-- Find reviewed replacements by page (for indexation comparison)
CREATE INDEX idx_reviewed_page ON reviewed (lang, page_id);

CREATE TABLE custom (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    lang CHAR(2) CHARACTER SET ascii NOT NULL,
    page_id MEDIUMINT UNSIGNED NOT NULL,
    replacement VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    cs TINYINT(1) NOT NULL,
    start MEDIUMINT UNSIGNED NOT NULL, -- So we can move items to the replacements table
    reviewer VARCHAR(40) NOT NULL,
    review_type TINYINT NOT NULL DEFAULT 0,
    review_timestamp TIMESTAMP,
    old_rev_id INTEGER UNSIGNED,
    new_rev_id INTEGER UNSIGNED,
    PRIMARY KEY (id)
);

-- FK custom --> page
ALTER TABLE custom
ADD CONSTRAINT fk_custom_page_id FOREIGN KEY (lang, page_id) REFERENCES page (lang, page_id) ON DELETE CASCADE;

-- FK custom --> review type
ALTER TABLE custom
  ADD CONSTRAINT fk_custom_review_type FOREIGN KEY (review_type) REFERENCES review_type (code);

-- Find reviewed pages by custom type
CREATE INDEX idx_custom_lookup ON custom (lang, replacement, cs, page_id);

-- Count reviewed grouped by reviewer
CREATE INDEX idx_custom_reviewer ON custom (lang, reviewer);
