package es.bvalero.replacer.finder.parser;

import static es.bvalero.replacer.finder.parser.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class ScannerClassic {

    private final List<Token> tokenList = new ArrayList<>();

    public Iterable<Token> scanTokens(String text) {
        // NOTE: Move these variables outside the method decreases the performance by 2x
        int current = 0;
        int leftText = -1;

        // NOTE: For some tokens we need to peek several characters ahead. The trick below is the best performant.
        while (current < text.length()) {
            final char c = text.charAt(current);
            if (c == '<') {
                if (current + 4 <= text.length()) {
                    if (
                        text.charAt(current + 1) == '!' &&
                        text.charAt(current + 2) == '-' &&
                        text.charAt(current + 3) == '-'
                    ) {
                        addTextToken(leftText, current);
                        leftText = -1;
                        addToken(START_COMMENT, current, current + 4);
                        current += 4;
                        continue;
                    }
                }
            } else if (c == '-') {
                if (current + 3 <= text.length()) {
                    if (text.charAt(current + 1) == '-' && text.charAt(current + 2) == '>') {
                        addTextToken(leftText, current);
                        leftText = -1;
                        addToken(END_COMMENT, current, current + 3);
                        current += 3;
                        continue;
                    }
                }
            }

            // Default
            if (leftText == -1) leftText = current;
            current++;
        }

        // Final text token
        assert current == text.length();
        addTextToken(leftText, current);

        return tokenList;
    }

    private void addTextToken(int leftText, int current) {
        if (leftText >= 0 && current > leftText) {
            addToken(TEXT, leftText, current);
        }
    }

    private void addToken(TokenType type, int start, int end) {
        tokenList.add(new Token(type, start, end));
    }
}
