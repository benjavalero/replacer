package es.bvalero.replacer.finder.parser;

import java.util.Collection;
import java.util.TreeSet;

import static es.bvalero.replacer.finder.parser.TokenType.*;

public class ScannerNoText {

    private final String source;

    public ScannerNoText(String source) {
        this.source = source;
    }

    public Collection<Token> scanTokens() {
        final Collection<Token> tokens = new TreeSet<>();

        // Add comment starts
        int start = 0;
        while (start >= 0) {
            start = source.indexOf("<!--", start);
            if (start >= 0) {
                tokens.add(new Token(START_COMMENT, start, start + 4));
                start += 4;
            }
        }

        // Add comment end
        start = 0;
        while (start >= 0) {
            start = source.indexOf("-->", start);
            if (start >= 0) {
                tokens.add(new Token(END_COMMENT, start, start + 3));
                start += 3;
            }
        }

        return tokens;
    }
}
