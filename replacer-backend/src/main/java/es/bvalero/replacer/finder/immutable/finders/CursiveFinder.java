package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find text in cursive and bold, e.g. `''cursive''` in `This is a ''cursive'' example`
 * It also finds text starting with the simple quotes and ending with a new line.
 */
@Component
class CursiveFinder extends ImmutableCheckedFinder {

    private static final String START_CURSIVE = "''";
    private static final char QUOTE = '\'';
    private static final char NEW_LINE = '\n';

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 2000;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findCursive);
    }

    private int findCursive(WikipediaPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        final int startCursive = findStartCursive(text, start);
        if (startCursive >= 0) {
            final int numQuotes = findNumQuotes(text, startCursive);
            assert numQuotes >= 2;
            final int startCursiveText = startCursive + numQuotes;
            final int endCursive = findEndCursive(text, startCursiveText, numQuotes);
            if (endCursive >= 0) {
                if (isEmptyCursiveText(text, startCursive, endCursive, numQuotes)) {
                    logImmutableCheck(page, startCursive, endCursive, "Empty cursive");
                } else {
                    matches.add(LinearMatchResult.of(startCursive, text.substring(startCursive, endCursive)));
                }
                return endCursive;
            } else {
                // No cursive ending found. Notify and continue.
                logImmutableCheck(page, startCursive, startCursiveText, "Truncated cursive");
                return startCursiveText;
            }
        } else {
            return -1;
        }
    }

    private int findStartCursive(String text, int start) {
        return text.indexOf(START_CURSIVE, start);
    }

    private int findNumQuotes(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (text.charAt(i) != QUOTE) {
                return i - start;
            }
        }
        // It shouldn't get here
        return 0;
    }

    private int findEndCursive(String text, int start, int numQuotes) {
        int numQuotesFound = 0;
        int i = start;
        for (; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (ch == NEW_LINE) {
                // New lines are considered as an ending for cursive
                return i;
            } else if (ch == QUOTE) {
                numQuotesFound++;
            } else {
                if (numQuotesFound > numQuotes) {
                    numQuotesFound = 0; // Reset and keep on searching
                } else if (numQuotesFound == numQuotes) {
                    return i;
                } else if (numQuotesFound > 0) {
                    numQuotesFound = 0; // Reset and keep on searching
                } // else (numQuotesFound == 0) ==> continue
            }
        }

        // In case we arrive at the end of the text
        return numQuotesFound == numQuotes ? i : -1;
    }

    private boolean isEmptyCursiveText(String text, int startCursive, int endCursive, int numQuotes) {
        // The cursive may end with the text end or with a new line
        final int endCursiveText = text.length() == endCursive || text.charAt(endCursive) == NEW_LINE
            ? endCursive
            : endCursive - numQuotes;
        final String cursiveText = text.substring(startCursive + numQuotes, endCursiveText);
        return StringUtils.isBlank(cursiveText);
    }
}
