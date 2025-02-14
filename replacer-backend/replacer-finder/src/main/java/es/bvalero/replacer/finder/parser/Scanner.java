package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.TokenType.*;

import java.util.*;

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

        // Assume the gaps between tokens is text
        addTextTokens(text, tokens);

        return tokens;
    }

    private void findTokens(String text, TokenType... types) {
        for (TokenType type : types) {
            int start = 0;
            while (start >= 0) {
                start = text.indexOf(type.literal(), start);
                if (start >= 0) {
                    final int end = start + type.literal().length();
                    addToken(type, start, end);
                    start = end;
                }
            }
        }
    }

    private void addToken(TokenType type, int start, int end) {
        if (end > start) {
            tokens.add(new Token(type, start, end));
        }
    }

    private void addTextTokens(String text, Collection<Token> tokens) {
        final List<Token> tokenList = new ArrayList<>(tokens);
        if (!tokenList.isEmpty()) {
            addToken(TEXT, 0, tokenList.get(0).start());
        }
        for (int i = 0; i < tokenList.size(); i++) {
            final int start = tokenList.get(i).end();
            final int end = (i == tokenList.size() - 1) ? text.length() : tokenList.get(i + 1).start();
            addToken(TEXT, start, end);
        }
    }
}
