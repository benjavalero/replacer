package es.bvalero.replacer.finder.parser;

import java.util.ArrayList;
import java.util.List;

public class ScannerSimple {

    private final String text;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;

    public ScannerSimple(String text) {
        this.text = text;
    }

    public List<Token> scanTokens() {
        while (start >= 0 && start < text.length()) {
            char ch = text.charAt(start++);
            if (ch == '<' && match('!') && match1('-') && match2('-')) {
                tokens.add(new Token(TokenType.START_COMMENT, start - 1, start + 3));
                start += 3;
            } else if (ch == '-' && match('-') && match1('>')) {
                tokens.add(new Token(TokenType.END_COMMENT, start - 1, start + 2));
                start += 2;
            } else {
                // Do nothing
            }
        }

        return tokens;
    }

    private boolean match(char ch) {
        return start < text.length() && text.charAt(start) == ch;
    }

    private boolean match1(char ch) {
        return start + 1 < text.length() && text.charAt(start + 1) == ch;
    }

    private boolean match2(char ch) {
        return start + 2 < text.length() && text.charAt(start + 2) == ch;
    }
}
