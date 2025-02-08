package es.bvalero.replacer.finder.parser;

enum TokenType {
    // Single-character tokens
    COLON, EQUALS, QUOTES,

    // One or more character tokens
    PIPE, START_ROW,
    LEFT_BRACKET, OPEN_LINK,
    RIGHT_BRACKET, CLOSE_LINK,
    LESS, START_CLOSE_TAG, OPEN_COMMENT,
    GREATER, CLOSE_COMMENT,
    OPEN_TEMPLATE, OPEN_TABLE,
    CLOSE_TEMPLATE,
    CLOSE_TAG,

    // Literals
    TEXT,
    IDENTIFIER, STRING, NUMBER,
}
