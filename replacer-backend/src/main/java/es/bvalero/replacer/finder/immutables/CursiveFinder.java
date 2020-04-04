package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import java.util.*;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find text in cursive, e. g. `''cursive''` in `This is a ''cursive'' example`
 */
@Component
public class CursiveFinder implements ImmutableFinder {
    private static final Set<Character> FORBIDDEN_CHARS = new HashSet<>(Arrays.asList('\n', '#', '{', '}', '<', '>'));

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MEDIUM;
    }

    @Override
    public int getMaxLength() {
        return 500;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new LinearIterable<>(text, this::findResult, this::convert);
    }

    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findCursive(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findCursive(String text, int start, List<MatchResult> matches) {
        int startCursive = findStartCursive(text, start);
        if (startCursive >= 0) {
            String allQuotes = findAllQuotes(text, startCursive);
            if (allQuotes != null) { // Just in case
                int endQuotes = findEndQuotes(text, startCursive + allQuotes.length(), allQuotes);
                if (endQuotes >= 0) {
                    int end = endQuotes + allQuotes.length();
                    matches.add(LinearMatcher.of(startCursive, text.substring(startCursive, end)));
                    return end;
                } else {
                    return startCursive + allQuotes.length();
                }
            } else {
                return startCursive + 2;
            }
        } else {
            return -1;
        }
    }

    private int findStartCursive(String text, int start) {
        return text.indexOf("''", start);
    }

    private String findAllQuotes(String text, int start) {
        StringBuilder tagBuilder = new StringBuilder();
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\'') {
                tagBuilder.append(ch);
            } else {
                break;
            }
        }
        String tag = tagBuilder.toString();
        return tag.length() >= 2 ? tag : null;
    }

    private int findEndQuotes(String text, int start, String allQuotes) {
        int endQuotes = text.indexOf(allQuotes, start);
        // Check there are no more quotes after
        char c = text.charAt(endQuotes + allQuotes.length());
        if (c == '\'') {
            endQuotes = findEndQuotes(text, endQuotes + allQuotes.length() + 1, allQuotes);
        }

        if (endQuotes >= 0) {
            // Check if the found text contains any forbidden char
            for (int i = start; i < endQuotes; i++) {
                char ch = text.charAt(i);
                if (FORBIDDEN_CHARS.contains(ch)) {
                    return -1;
                }
            }
        }
        return endQuotes;
    }
}
