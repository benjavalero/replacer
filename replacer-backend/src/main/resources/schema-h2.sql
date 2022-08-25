CREATE TABLE IF NOT EXISTS lang (
    code CHAR(2) NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT constraint_l PRIMARY KEY (code)
);

INSERT INTO lang (code, name) VALUES ('es', 'SPANISH');
INSERT INTO lang (code, name) VALUES ('gl', 'GALICIAN');

CREATE TABLE IF NOT EXISTS page (
    lang CHAR(2) NOT NULL,
    page_id INTEGER NOT NULL,
    last_update DATE NOT NULL,
    title VARCHAR(255) NOT NULL,
    CONSTRAINT constraint_p PRIMARY KEY (lang, page_id)
);

-- FK page --> lang
ALTER TABLE page
ADD CONSTRAINT fk_page_lang FOREIGN KEY (lang) REFERENCES lang (code);

CREATE TABLE IF NOT EXISTS replacement_kind (
    code TINYINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT constraint_k PRIMARY KEY (code)
);

INSERT INTO replacement_kind (code, name) VALUES (2, 'SIMPLE');
INSERT INTO replacement_kind (code, name) VALUES (3, 'COMPOSED');
INSERT INTO replacement_kind (code, name) VALUES (4, 'DATE');
INSERT INTO replacement_kind (code, name) VALUES (5, 'STYLE');

CREATE TABLE IF NOT EXISTS replacement (
	id INTEGER NOT NULL AUTO_INCREMENT,
	lang CHAR(2) NOT NULL,
	page_id INTEGER NOT NULL,
	kind TINYINT NOT NULL,
	subtype VARCHAR(100) NOT NULL,
	start INTEGER NOT NULL,
	context VARCHAR(255) NOT NULL,
	reviewer VARCHAR(100),
	CONSTRAINT constraint_r PRIMARY KEY (id)
);

-- FK replacement --> page
ALTER TABLE replacement
ADD CONSTRAINT fk_page_id FOREIGN KEY (lang, page_id) REFERENCES page (lang, page_id) ON DELETE CASCADE;

-- FK replacement --> kind
ALTER TABLE replacement
ADD CONSTRAINT fk_replacement_kind FOREIGN KEY (kind) REFERENCES replacement_kind (code);

CREATE INDEX IF NOT EXISTS idx_count ON replacement (lang, reviewer, kind, subtype);
CREATE INDEX IF NOT EXISTS idx_count_no_type ON replacement (lang, reviewer);
CREATE INDEX IF NOT EXISTS idx_reviewer ON replacement (reviewer);
CREATE INDEX IF NOT EXISTS idx_dump ON replacement (lang, page_id, reviewer);

CREATE TABLE IF NOT EXISTS custom (
	id INTEGER NOT NULL AUTO_INCREMENT,
	lang CHAR(2) NOT NULL,
	page_id INTEGER NOT NULL,
	replacement VARCHAR(100) NOT NULL,
	cs TINYINT NOT NULL,
	start INTEGER NOT NULL,
	reviewer VARCHAR(100) NOT NULL,
	CONSTRAINT constraint_c PRIMARY KEY (id)
);

-- FK custom --> page
ALTER TABLE custom
ADD CONSTRAINT fk_custom_page_id FOREIGN KEY (lang, page_id) REFERENCES page (lang, page_id) ON DELETE CASCADE;
