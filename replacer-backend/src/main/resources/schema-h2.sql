CREATE TABLE IF NOT EXISTS page (
    lang CHAR(2) NOT NULL,
    article_id INTEGER NOT NULL,
    last_update DATE NOT NULL,
    title VARCHAR(255) NOT NULL,
    CONSTRAINT constraint_p PRIMARY KEY (lang, article_id)
);

CREATE TABLE IF NOT EXISTS replacement (
	id INTEGER NOT NULL AUTO_INCREMENT,
	lang CHAR(2) NOT NULL,
	article_id INTEGER NOT NULL,
	type TINYINT NOT NULL,
	subtype VARCHAR(100) NOT NULL,
	position INTEGER NOT NULL DEFAULT 0,
	context VARCHAR(255) NOT NULL,
	reviewer VARCHAR(100),
	CONSTRAINT constraint_r PRIMARY KEY (id)
);

ALTER TABLE replacement
ADD CONSTRAINT fk_page_id FOREIGN KEY (lang, article_id) REFERENCES page (lang, article_id);

CREATE INDEX IF NOT EXISTS idx_count ON replacement (lang, reviewer, type, subtype);
CREATE INDEX IF NOT EXISTS idx_count_no_type ON replacement (lang, reviewer);
CREATE INDEX IF NOT EXISTS idx_reviewer ON replacement (reviewer);
CREATE INDEX IF NOT EXISTS idx_dump ON replacement (lang, article_id, reviewer);

CREATE TABLE IF NOT EXISTS custom (
	id INTEGER NOT NULL AUTO_INCREMENT,
	lang CHAR(2) NOT NULL,
	article_id INTEGER NOT NULL,
	replacement VARCHAR(100) NOT NULL,
	cs TINYINT NOT NULL DEFAULT 0,
	position INTEGER NOT NULL DEFAULT 0,
	reviewer VARCHAR(100),
	CONSTRAINT constraint_c PRIMARY KEY (id)
);

ALTER TABLE custom
ADD CONSTRAINT fk_custom_page_id FOREIGN KEY (lang, article_id) REFERENCES page (lang, article_id);
