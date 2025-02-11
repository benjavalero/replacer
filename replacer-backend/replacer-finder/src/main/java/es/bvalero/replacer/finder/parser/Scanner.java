package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.TokenType.END_COMMENT;
import static es.bvalero.replacer.finder.parser.TokenType.START_COMMENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Scanner {

    private final String text;
    private final Set<Token> tokens = new TreeSet<>();
    private int current = 0;

    public Scanner(String text) {
        this.text = text;
    }

    public List<Token> scanTokens() {
        /*
        // Here we scan simple tokens
        while (!isAtEnd()) {
            char c = advance();
            switch (c) {
                case '<':
                    break;
                case '-':
                    break;
                default:
                    // For the moment we don't treat text
                    current++;
                    break;
            }
        }
        */

        // Here we scan large known tokens just by finding them in the text as it is way more performant
        findTokens(START_COMMENT, END_COMMENT);

        return new ArrayList<>(tokens);
    }

    private boolean isAtEnd() {
        return current >= text.length();
    }

    private char advance() {
        return text.charAt(current++);
    }

    private void findTokens(TokenType... types) {
        for (TokenType type : types) {
            findToken(type);
        }
    }

    private void findToken(TokenType type) {
        int start = 0;
        while (start >= 0) {
            start = text.indexOf(type.literal(), start);
            if (start >= 0) {
                final int end = start + type.literal().length();
                tokens.add(new Token(type, start, end));
                start = end;
            }
        }
    }
}
