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
    status VARCHAR(10) NOT NULL DEFAULT 'TO_REVIEW',
    last_update DATE NOT NULL DEFAULT NOW(),
    type VARCHAR,
    PRIMARY KEY (id),
    INDEX (articleId),
    INDEX (status),
    INDEX (type, subtype)
);
