package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.*;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

abstract class QuotesFinder extends ImmutableCheckedFinder {

    private static final char NEW_LINE = '\n';
    private static final Set<Character> QUOTE_CHARS = Set.of('«', '»', '"', '“', '”');

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 1000;
    }

    abstract char getStartChar();

    abstract char getEndChar();

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findResult);
    }

    @Nullable
    private MatchResult findResult(FinderPage page, int start) {
        final List<MatchResult> matches = new ArrayList<>();
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findQuote(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findQuote(FinderPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        final int startQuote = findStartQuote(text, start);
        if (startQuote >= 0) {
            int endQuote = findEndQuote(text, startQuote + 1);
            if (endQuote >= 0) {
                endQuote++; // Move to the position next to the end of the quote

                final String quotedText = text.substring(startQuote, endQuote);
                final String innerText = quotedText.substring(1, quotedText.length() - 1);

                if (!validateForbiddenChars(quotedText)) {
                    // Quote containing forbidden chars. Continue.
                    return endQuote;
                }

                // Check the quoted text is not empty and is not an attribute
                if (StringUtils.isBlank(innerText) && text.charAt(startQuote - 1) != '=') {
                    logImmutableCheck(page, startQuote, endQuote, "Empty quoted text");
                    return endQuote;
                }

                // Check the quoted text is not quoted again
                if (
                    StringUtils.isNotBlank(innerText) &&
                    QUOTE_CHARS.contains(innerText.charAt(0)) &&
                    QUOTE_CHARS.contains(innerText.charAt(innerText.length() - 1))
                ) {
                    logImmutableCheck(page, startQuote, endQuote, "Redundant quotes");
                    // Continue
                }

                matches.add(LinearMatchResult.of(startQuote, quotedText));
                return endQuote;
            } else {
                // No quote ending found
                // It's possible that the quote start was a false positive or the quote contains new lines
                logImmutableCheck(page, startQuote, startQuote + 1, "Truncated quotes");
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
        for (int i = start; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (ch == NEW_LINE) {
                // New lines are not allowed inside quotes
                return -1;
            } else if (ch == getEndChar()) {
                return i;
            }
        }
        return -1;
    }

    /* Check if the found text contains any forbidden char */
    private boolean validateForbiddenChars(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (getForbiddenChars().contains(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    Collection<Character> getForbiddenChars() {
        return Collections.emptySet();
    }
}
