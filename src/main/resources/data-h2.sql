INSERT INTO article (id, title, dtadd) VALUES (1, 'Andorra', NOW());
INSERT INTO article (id, title, dtadd) VALUES (2, 'España', NOW());
INSERT INTO article (id, title, dtadd) VALUES (3, 'Orihuela', NOW());

INSERT INTO replacement (articleid, type, text) VALUES (1, 'MISSPELLING', 'aber');
INSERT INTO replacement (articleid, type, text) VALUES (2, 'MISSPELLING', 'aber');
INSERT INTO replacement (articleid, type, text) VALUES (2, 'MISSPELLING', 'madrid');
INSERT INTO replacement (articleid, type, text) VALUES (2, 'MISSPELLING', 'paris');
