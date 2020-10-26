CREATE TABLE IF NOT EXISTS REPLACEMENT2 (
	ID BIGINT NOT NULL AUTO_INCREMENT,
	CONTEXT VARCHAR(255),
	LANG VARCHAR(2),
	LAST_UPDATE DATE NOT NULL,
	ARTICLE_ID INTEGER,
	"POSITION" INTEGER DEFAULT 0,
	REVIEWER VARCHAR(255),
	SUBTYPE VARCHAR(30) NOT NULL,
	TITLE VARCHAR(255),
	"TYPE" VARCHAR(25) NOT NULL,
	CONSTRAINT CONSTRAINT_A PRIMARY KEY (ID)
);
