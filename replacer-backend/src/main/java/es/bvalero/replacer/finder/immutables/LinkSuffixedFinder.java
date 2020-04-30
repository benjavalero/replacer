package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.springframework.stereotype.Component;

/**
 * Find links with suffix, e. g. `[[brasil]]eño`
 */
@Component
public class LinkSuffixedFinder implements ImmutableFinder {

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 500;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return new LinearIterable<>(text, this::findResult, this::convert);
    }

    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findLink(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findLink(String text, int start, List<MatchResult> matches) {
        int startLink = findStartLink(text, start);
        if (startLink >= 0) {
            int endLink = findEndLink(text, startLink + 2);
            if (endLink >= 0) {
                matches.add(LinearMatcher.of(startLink, text.substring(startLink, endLink + 1)));
                return endLink + 1;
            } else {
                return startLink + 1;
            }
        } else {
            return -1;
        }
    }

    private int findStartLink(String text, int start) {
        return text.indexOf("[[", start);
    }

    private int findEndLink(String text, int start) {
        int endQuote = text.indexOf("]]", start);
        if (endQuote >= 0) {
            // Move forward until there are letters
            for (int i = endQuote + 2; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (!Character.isLowerCase(ch)) {
                    return i == endQuote + 2 ? -1 : i - 1;
                }
            }
        }
        return endQuote;
    }
}
