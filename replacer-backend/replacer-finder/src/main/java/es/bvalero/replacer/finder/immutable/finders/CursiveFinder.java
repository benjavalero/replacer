package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.NEW_LINE;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find text in cursive and bold, e.g. `''cursive''` in `This is a ''cursive'' example`
 * It also finds text starting with the simple quotes and ending with a new line.
 */
@Component
class CursiveFinder extends ImmutableCheckedFinder {

    private static final String START_CURSIVE = "''";
    private static final char QUOTE = '\'';

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 2000;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        // The best option is the linear approach, in order to check truncated or empty cases,
        // which lead to complicated regex. In comparison, the automaton approach is 10x slower.
        return LinearMatchFinder.find(page, this::findCursive);
    }

    @Nullable
    private MatchResult findCursive(FinderPage page, int start) {
        final String text = page.getContent();
        // TODO: Reduce cyclomatic complexity
        while (start >= 0 && start < text.length()) {
            final int startCursive = findStartCursive(text, start);
            if (startCursive < 0) {
                return null;
            }

            final int numQuotes = findNumQuotes(text, startCursive);
            assert numQuotes >= 2;
            final int startCursiveText = startCursive + numQuotes;
            final int endCursive = findEndCursive(text, startCursiveText, numQuotes);
            if (endCursive < 0) {
                // No cursive ending found. Notify and continue.
                logImmutableCheck(page, startCursive, startCursiveText, "Truncated cursive");
                start = startCursiveText;
                continue;
            }

            // Additional validation
            if (isEmptyCursiveText(text, startCursive, endCursive, numQuotes)) {
                logImmutableCheck(page, startCursive, endCursive, "Empty cursive");
                start = endCursive;
                continue;
            }

            return FinderMatchResult.of(text, startCursive, endCursive);
        }
        return null;
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
        return text.length() - start;
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
        final int startCursiveText = startCursive + numQuotes;
        final int endCursiveText = text.length() == endCursive || text.charAt(endCursive) == NEW_LINE
            ? endCursive
            : endCursive - numQuotes;
        final String cursiveText = text.substring(startCursiveText, endCursiveText);
        return StringUtils.isBlank(cursiveText);
    }
}
