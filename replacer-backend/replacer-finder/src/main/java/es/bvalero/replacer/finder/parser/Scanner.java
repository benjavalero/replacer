package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.TokenType.END_COMMENT;
import static es.bvalero.replacer.finder.parser.TokenType.START_COMMENT;

import java.util.Set;
import java.util.TreeSet;

public class Scanner {

    final Set<Token> tokens = new TreeSet<>();

    public Iterable<Token> scanTokens(String text) {
        /*
        // Here we scan simple tokens
        int current = 0;
        while (current < text.length()) {
            final char c = text.charAt(current++);
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
        findTokens(text, START_COMMENT, END_COMMENT);

        return tokens;
    }

    private void findTokens(String text, TokenType... types) {
        for (TokenType type : types) {
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
}
