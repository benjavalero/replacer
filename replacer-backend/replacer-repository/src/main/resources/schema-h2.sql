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
INSERT INTO replacement_kind (code, name) VALUES (5, 'STYLE');

CREATE TABLE IF NOT EXISTS review_type (
    code TINYINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT constraint_rt PRIMARY KEY (code)
);

INSERT INTO review_type (code, name) VALUES (0, 'UNKNOWN');
INSERT INTO review_type (code, name) VALUES (1, 'MODIFIED');
INSERT INTO review_type (code, name) VALUES (2, 'NOT MODIFIED');
INSERT INTO review_type (code, name) VALUES (3, 'IGNORED');

CREATE TABLE IF NOT EXISTS replacement (
	id INTEGER NOT NULL AUTO_INCREMENT,
	lang CHAR(2) NOT NULL,
	page_id INTEGER NOT NULL,
	kind TINYINT NOT NULL,
	subtype VARCHAR(100) NOT NULL,
	start INTEGER NOT NULL,
	context VARCHAR(255) NOT NULL,
	reviewer VARCHAR(40),
  review_type TINYINT DEFAULT 0 NOT NULL,
  review_timestamp TIMESTAMP,
  old_rev_id INTEGER,
  new_rev_id INTEGER,
	CONSTRAINT constraint_r PRIMARY KEY (id)
);

-- FK replacement --> page
ALTER TABLE replacement
ADD CONSTRAINT fk_page_id FOREIGN KEY (lang, page_id) REFERENCES page (lang, page_id) ON DELETE CASCADE;

-- FK replacement --> kind
ALTER TABLE replacement
ADD CONSTRAINT fk_replacement_kind FOREIGN KEY (kind) REFERENCES replacement_kind (code);

-- FK replacement --> review type
ALTER TABLE replacement
  ADD CONSTRAINT fk_review_type FOREIGN KEY (review_type) REFERENCES review_type (code);

-- Find and count pages to review by subtype (or no type)
-- According to the explain plan, we need to put the reviewer at first to use the index.
CREATE INDEX IF NOT EXISTS idx_count ON replacement (reviewer, lang, kind, subtype, page_id);

CREATE TABLE IF NOT EXISTS custom (
	id INTEGER NOT NULL AUTO_INCREMENT,
	lang CHAR(2) NOT NULL,
	page_id INTEGER NOT NULL,
	replacement VARCHAR(100) NOT NULL,
	cs TINYINT NOT NULL,
	start INTEGER NOT NULL, -- So we can move items to the replacements table
	reviewer VARCHAR(40) NOT NULL,
  review_type TINYINT DEFAULT 0 NOT NULL,
  review_timestamp TIMESTAMP,
  old_rev_id INTEGER,
  new_rev_id INTEGER,
	CONSTRAINT constraint_c PRIMARY KEY (id)
);

-- FK custom --> page
ALTER TABLE custom
ADD CONSTRAINT fk_custom_page_id FOREIGN KEY (lang, page_id) REFERENCES page (lang, page_id) ON DELETE CASCADE;

-- FK custom --> review type
ALTER TABLE custom
  ADD CONSTRAINT fk_custom_review_type FOREIGN KEY (review_type) REFERENCES review_type (code);
