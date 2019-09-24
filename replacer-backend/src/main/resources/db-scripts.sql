DROP TABLE IF EXISTS replacement;
DROP TABLE IF EXISTS article;

-- We cannot create an index or unique constraint for the title because the column is too long
CREATE TABLE article (
    id INT NOT NULL,
    title VARCHAR(255) COLLATE utf8mb4_bin NOT NULL,
    lastupdate DATE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE replacement (
    id INT NOT NULL AUTO_INCREMENT,
    articleid INT NOT NULL,
    type VARCHAR(25) NOT NULL,
    text VARCHAR(30) COLLATE utf8mb4_bin NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (articleid, type, text),
    INDEX (type),
    INDEX (text),
    FOREIGN KEY (articleid) REFERENCES article(id)
);

DROP TABLE IF EXISTS replacement2;

CREATE TABLE replacement2 (
    id INT NOT NULL AUTO_INCREMENT,
    article_id INT NOT NULL,
    type VARCHAR(25) NOT NULL,
    subtype VARCHAR(30) COLLATE utf8mb4_bin NOT NULL,
    position INT NOT NULL DEFAULT 0,
    last_update DATE NOT NULL,
    reviewer VARCHAR(255),
    PRIMARY KEY (id),
    INDEX (article_id),
    INDEX (status),
    INDEX (type, subtype)
);

ALTER TABLE replacement2 ADD CONSTRAINT same_rep UNIQUE KEY(article_id, type, subtype, position);

DROP TABLE IF EXISTS indexation;

CREATE TABLE indexation (
    id INT NOT NULL AUTO_INCREMENT,
    force_process BIT(1) NOT NULL,
    num_articles_read INT NOT NULL,
    num_articles_processable INT NOT NULL,
    num_articles_processed INT NOT NULL,
    dump_file_name VARCHAR(255) NOT NULL,
    start INT NOT NULL,
    end INT,
    PRIMARY KEY (id)
);