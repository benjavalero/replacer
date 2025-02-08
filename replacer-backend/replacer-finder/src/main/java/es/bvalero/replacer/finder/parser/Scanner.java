package es.bvalero.replacer.finder.parser;

import java.util.ArrayList;
import java.util.List;

import static es.bvalero.replacer.finder.parser.TokenType.*;

class Scanner {
/*
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }
 */

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
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        // addToken(EOF, null);
        addTextToken();
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case ':': addToken(COLON); break;
            case '>': addToken(GREATER); break;
            case '=': addToken(EQUALS); break;
            case '"': addToken(QUOTES); break;

            case '|':
                addToken(match('-') ? START_ROW : PIPE);
                break;
            case '[':
                addToken(match('[') ? OPEN_LINK : LEFT_BRACKET);
                break;
            case ']':
                addToken(match('=') ? CLOSE_LINK : RIGHT_BRACKET);
                break;
            case '<':
                if (match('/')) {
                    addToken(START_CLOSE_TAG);
                } else {
                    addToken(match("!--") ? OPEN_COMMENT : LESS);
                }
                break;
            case '{':
                if (match('{')) {
                    addToken(OPEN_TEMPLATE);
                    break;
                } else if (match('|')) {
                    addToken(OPEN_TABLE);
                    break;
                }
                // If not treat the brace as a common character
            case '}':
                if (match('}')) {
                    addToken(CLOSE_TEMPLATE);
                    break;
                }
                // If not treat the brace as a common character
            case '-':
                if (match("->")) {
                    addToken(CLOSE_COMMENT);
                    break;
                }
                // If not treat the brace as a common character
            case '/':
                if (match('>')) {
                    addToken(CLOSE_TAG);
                    break;
                }
                // If not treat the slash as a common character
/*
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                line++;
                break;
*/
            default:
                text();
                /*
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    ScannerPoC.error(line, "Unexpected character.");
                }
                 */
                break;
        }
    }

    private void text() {
        if (startText < 0) startText = start;
    }

    /*
        private void identifier() {
            while (isAlphaNumeric(peek())) advance();

            String text = source.substring(start, current);
            TokenType type = keywords.get(text);
            if (type == null) type = IDENTIFIER;
            addToken(type);
        }

        private void number() {
            while (isDigit(peek())) advance();

            // Look for a fractional part.
            if (peek() == '.' && isDigit(peekNext())) {
                // Consume the "."
                advance();

                while (isDigit(peek())) advance();
            }

            addToken(NUMBER,
                Double.parseDouble(source.substring(start, current)));
        }

        private void string() {
            while (peek() != '"' && !isAtEnd()) {
                if (peek() == '\n') line++;
                advance();
            }

            if (isAtEnd()) {
                ScannerPoC.error(line, "Unterminated string.");
                return;
            }

            // The closing ".
            advance();

            // Trim the surrounding quotes.
            String value = source.substring(start + 1, current - 1);
            addToken(STRING, value);
        }
    */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private boolean match(String expected) {
        if (current + expected.length() > source.length()) return false;
        if (!source.startsWith(expected, current)) return false;

        current += expected.length();
        return true;
    }
/*
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
            (c >= 'A' && c <= 'Z') ||
            c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
*/
    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addTextToken();

        String text = source.substring(start, current);
        tokens.add(new Token(type, text, start));
    }

    private void addTextToken() {
        if (startText < 0) return;

        String text = isAtEnd()
            ? source.substring(startText)
            : source.substring(startText, start);
        tokens.add(new Token(TEXT, text, startText));

        startText = -1;
    }
}
