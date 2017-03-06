DROP TABLE IF EXISTS replacement;
DROP TABLE IF EXISTS misspelling;

CREATE TABLE misspelling (
	word varchar(190) COLLATE utf8mb4_bin NOT NULL,
	cs tinyint(1) NOT NULL DEFAULT '1',
	suggestion varchar(255) NOT NULL,
	PRIMARY KEY (word)
);

CREATE TABLE replacement (
	title varchar(190) COLLATE utf8mb4_bin NOT NULL,
	word varchar(190) COLLATE utf8mb4_bin NOT NULL,
	dtadded timestamp NULL,
	dtfixed timestamp NULL,
	INDEX (title, word, dtfixed)
);
