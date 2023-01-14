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

CREATE TABLE replacement (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    lang CHAR(2) CHARACTER SET ascii NOT NULL,
    page_id MEDIUMINT UNSIGNED NOT NULL,
    kind TINYINT NOT NULL,
    subtype VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    start MEDIUMINT UNSIGNED NOT NULL,
    context VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
    reviewer VARCHAR(100), -- In order to make the index work
    PRIMARY KEY (id)
);

-- FK replacement --> page
ALTER TABLE replacement
ADD CONSTRAINT fk_page_id FOREIGN KEY (lang, page_id) REFERENCES page (lang, page_id) ON DELETE CASCADE;

-- FK replacement --> kind
ALTER TABLE replacement
ADD CONSTRAINT fk_replacement_kind FOREIGN KEY (kind) REFERENCES replacement_kind (code);

-- To find random pages and count the group replacements
CREATE INDEX idx_count ON replacement (lang, reviewer, kind, subtype);
CREATE INDEX idx_count_no_type ON replacement (lang, reviewer);

-- Statistics
CREATE INDEX idx_reviewer ON replacement (reviewer);

-- Dump index
CREATE INDEX idx_dump ON replacement (lang, page_id, reviewer);

-- New table only for custom replacements
CREATE TABLE custom (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    lang CHAR(2) CHARACTER SET ascii NOT NULL,
    page_id MEDIUMINT UNSIGNED NOT NULL,
    replacement VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    cs TINYINT(1) NOT NULL,
    start MEDIUMINT UNSIGNED NOT NULL, -- So we can move items to the replacements table
    reviewer VARCHAR(100) NOT NULL, -- In order to make the index work
    PRIMARY KEY (id)
);

-- FK custom --> page
ALTER TABLE custom
ADD CONSTRAINT fk_custom_page_id FOREIGN KEY (lang, page_id) REFERENCES page (lang, page_id) ON DELETE CASCADE;