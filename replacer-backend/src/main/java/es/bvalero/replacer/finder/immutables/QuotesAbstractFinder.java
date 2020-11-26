package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.*;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

abstract class QuotesAbstractFinder implements ImmutableFinder {
    private static final Set<Character> FORBIDDEN_CHARS = new HashSet<>(Arrays.asList('\n', '#', '{', '}', '<', '>'));

    @Override
    public int getMaxLength() {
        return 500;
    }

    abstract char getStartChar();

    abstract char getEndChar();

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return new LinearIterable<>(text, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findQuote(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findQuote(String text, int start, List<MatchResult> matches) {
        int startQuote = findStartQuote(text, start);
        if (startQuote >= 0) {
            int endQuote = findEndQuote(text, startQuote + 1);
            if (endQuote >= 0) {
                matches.add(LinearMatcher.of(startQuote, text.substring(startQuote, endQuote + 1)));
                return endQuote + 1;
            } else {
                return startQuote + 1;
            }
        } else {
            return -1;
        }
    }

    private int findStartQuote(String text, int start) {
        return text.indexOf(getStartChar(), start);
    }

    private int findEndQuote(String text, int start) {
        int endQuote = text.indexOf(getEndChar(), start);
        if (endQuote >= 0) {
            // Check if the found text contains any forbidden char
            for (int i = start; i < endQuote; i++) {
                char ch = text.charAt(i);
                if (FORBIDDEN_CHARS.contains(ch)) {
                    return -1;
                }
            }
        }
        return endQuote;
    }
}
