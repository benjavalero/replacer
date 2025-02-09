package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.TokenType.*;

import java.util.ArrayList;
import java.util.List;

class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int startText = -1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        addTextToken();
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '<':
                if (match("!--")) {
                    addToken(START_COMMENT, "<!--");
                    break;
                }
            // If not treat the brace as a common character
            case '-':
                if (match("->")) {
                    addToken(END_COMMENT, "-->");
                    break;
                }
            // If not treat the brace as a common character
            default:
                text();
                break;
        }
    }

    private char advance() {
        return source.charAt(current++);
    }

    /*
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }
     */

    private boolean match(String expected) {
        if (current + expected.length() > source.length()) return false;
        if (!source.startsWith(expected, current)) return false;

        current += expected.length();
        return true;
    }

    private void addToken(TokenType type, String text) {
        addTextToken();

        tokens.add(new Token(type, start, text));
    }

    private void addTextToken() {
        if (startText < 0) return;

        String text = isAtEnd() ? source.substring(startText) : source.substring(startText, start);
        tokens.add(new Token(TEXT, startText, text));

        startText = -1;
    }

    private void text() {
        if (startText < 0) startText = start;
    }
}
