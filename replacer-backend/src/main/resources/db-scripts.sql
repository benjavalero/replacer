DROP TABLE IF EXISTS replacement2;

CREATE TABLE replacement2 (
    id INT NOT NULL AUTO_INCREMENT,
    article_id INT NOT NULL,
    type VARCHAR(25) NOT NULL,
    subtype VARCHAR(30) COLLATE utf8mb4_bin NOT NULL,
    position INT NOT NULL DEFAULT 0,
    context VARCHAR(255),
    last_update DATE NOT NULL,
    reviewer VARCHAR(255),
    PRIMARY KEY (id),
    INDEX (article_id),
    INDEX (type, subtype)
);
