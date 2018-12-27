DROP TABLE IF EXISTS replacement;
DROP TABLE IF EXISTS article;

-- We cannot create an index or unique constraint for the title because the column is too long
CREATE TABLE article (
    id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    dtadd TIMESTAMP NOT NULL,
    dtreview TIMESTAMP NULL,
    PRIMARY KEY (id)
);

CREATE TABLE replacement (
    id INT NOT NULL AUTO_INCREMENT,
    articleid INT NOT NULL,
    type VARCHAR(25) NOT NULL,
    text VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (articleid, type, text),
    INDEX (type),
    INDEX (text),
    FOREIGN KEY (articleid) REFERENCES article(id)
);
