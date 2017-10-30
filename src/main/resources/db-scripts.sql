DROP TABLE IF EXISTS replacement;
DROP TABLE IF EXISTS misspelling;
DROP TABLE IF EXISTS potentialerror;
DROP TABLE IF EXISTS article;

CREATE TABLE article (
    id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    dtadd TIMESTAMP NOT NULL,
    dtreview TIMESTAMP NULL,
    PRIMARY KEY (id),
    INDEX (dtreview)
);

CREATE TABLE potentialerror (
    id INT NOT NULL AUTO_INCREMENT,
    articleid INT NOT NULL,
    type VARCHAR(25) NOT NULL,
    text VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    INDEX (articleid, type),
    FOREIGN KEY (articleid) REFERENCES article(id)
);
