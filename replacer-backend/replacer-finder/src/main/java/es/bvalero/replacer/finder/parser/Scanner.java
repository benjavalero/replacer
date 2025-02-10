package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int startText = -1;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        // Add a possible remaining text token at the end
        if (startText >= 0) {
            tokens.add(new Token(TEXT, startText, source.length()));
        }

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
                    addToken(START_COMMENT, "<!--".length());
                    break;
                }
            // If not treat the brace as a common character
            case '-':
                if (match("->")) {
                    addToken(END_COMMENT, "-->".length());
                    break;
                }
            // If not treat the brace as a common character
            default:
                treatText();
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

    private void addToken(TokenType type, int length) {
        addTextToken();

        tokens.add(new Token(type, start, start + length));
    }

    private void addTextToken() {
        if (startText >= 0) {
            tokens.add(new Token(TEXT, startText, start));
            startText = -1;
        }
    }

    private void treatText() {
        if (startText < 0) {
            startText = start;
        }
    }
}
