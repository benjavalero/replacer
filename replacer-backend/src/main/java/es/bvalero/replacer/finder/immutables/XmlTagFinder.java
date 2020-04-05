package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;
import java.util.*;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find XML tags, e. g. `<span>` or `<br />`
 */
@Component
public class XmlTagFinder implements ImmutableFinder {
    // We want to avoid the XML comments to be captured by this
    private static final Set<Character> FORBIDDEN_CHARS = new HashSet<>(Arrays.asList('#', '{', '}', '<', '>'));

    @Override
    public int getMaxLength() {
        return 300;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new LinearIterable<>(text, this::findResult, this::convert);
    }

    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findTag(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findTag(String text, int start, List<MatchResult> matches) {
        int startTag = findStartTag(text, start);
        if (startTag >= 0) {
            char first = text.charAt(startTag + 1);
            if (Character.isLetter(first) || first == '/') {
                int endTag = findEndTag(text, startTag + 1);
                if (endTag >= 0) {
                    matches.add(LinearMatcher.of(startTag, text.substring(startTag, endTag + 1)));
                    return endTag + 1;
                } else {
                    return startTag + 1;
                }
            } else {
                return startTag + 1;
            }
        } else {
            return -1;
        }
    }

    private int findStartTag(String text, int start) {
        return text.indexOf('<', start);
    }

    private int findEndTag(String text, int start) {
        int endQuote = text.indexOf('>', start);
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
