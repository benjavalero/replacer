package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.page.IndexablePage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find links with suffix, e.g. `[[brasil]]eño`
 */
@Component
public class LinkSuffixedFinder implements ImmutableFinder {

    private static final String START_LINK = "[[";
    private static final String END_LINK = "]]";

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 100;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new LinearIterable<>(page, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findLink(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findLink(String text, int start, List<MatchResult> matches) {
        int startLink = findStartLink(text, start);
        if (startLink >= 0) {
            int startLinkText = startLink + START_LINK.length();
            int endLink = findEndLink(text, startLinkText);
            if (endLink >= 0) {
                // Skip links containing a colon
                // Maybe we skip some annex but it is worth for most cases
                if (text.substring(startLinkText, endLink).contains(":")) {
                    return startLinkText;
                }

                // Move forward until there are letters
                int startSuffix = endLink + END_LINK.length();
                int endSuffix = findEndSuffix(text, startSuffix);
                if (endSuffix > startSuffix) {
                    matches.add(LinearMatcher.of(startLink, text.substring(startLink, endSuffix)));
                }
                return endSuffix + 1;
            } else {
                // Link end not found
                return startLink + START_LINK.length();
            }
        } else {
            return -1;
        }
    }

    private int findStartLink(String text, int start) {
        return text.indexOf(START_LINK, start);
    }

    private int findEndLink(String text, int start) {
        return text.indexOf(END_LINK, start);
    }

    private int findEndSuffix(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (!Character.isLowerCase(text.charAt(i))) {
                return i;
            }
        }
        // In case we reach the end of the text
        return text.length();
    }
}
