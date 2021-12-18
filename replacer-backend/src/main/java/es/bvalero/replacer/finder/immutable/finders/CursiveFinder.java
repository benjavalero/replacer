package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find text in cursive and bold, e.g. `''cursive''` in `This is a ''cursive'' example`
 * It also finds text starting with the simple quotes and ending with a new line.
 */
@Component
class CursiveFinder extends ImmutableCheckedFinder {

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 2000;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findResult);
    }

    @Nullable
    private MatchResult findResult(FinderPage page, int start) {
        List<MatchResult> matches = new ArrayList<>();
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findCursive(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findCursive(FinderPage page, int start, List<MatchResult> matches) {
        String text = page.getContent();
        int startCursive = findStartCursive(text, start);
        if (startCursive >= 0) {
            int numQuotes = findNumQuotes(text, startCursive);
            assert numQuotes >= 2;
            int endQuotes = findEndQuotes(text, startCursive + numQuotes, numQuotes);
            if (endQuotes >= 0) {
                // Check if the content of the cursive is empty. Notify and continue in such a case.
                int endCursiveText = text.length() == endQuotes || text.charAt(endQuotes) == '\n'
                    ? endQuotes
                    : endQuotes - numQuotes;
                if (StringUtils.isBlank(text.substring(startCursive + numQuotes, endCursiveText))) {
                    logImmutableCheck(getImmutableSnippet(startCursive, endQuotes, page), "Empty cursive");
                } else {
                    matches.add(LinearMatchResult.of(startCursive, text.substring(startCursive, endQuotes)));
                }
                return endQuotes;
            } else {
                // No cursive ending found. Notify and continue.
                logImmutableCheck(
                    getImmutableSnippet(startCursive, startCursive + numQuotes, page),
                    "Truncated cursive"
                );
                return startCursive + numQuotes;
            }
        } else {
            return -1;
        }
    }

    private int findStartCursive(String text, int start) {
        return text.indexOf("''", start);
    }

    private int findNumQuotes(String text, int start) {
        StringBuilder quotesBuilder = new StringBuilder();
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\'') {
                quotesBuilder.append(ch);
            } else {
                break;
            }
        }
        return quotesBuilder.length();
    }

    private int findEndQuotes(String text, int start, int numQuotes) {
        StringBuilder tagBuilder = new StringBuilder();
        int i = start;
        for (; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                // New lines are considered as an ending for cursive
                return i;
            } else if (ch == '\'') {
                tagBuilder.append(ch);
            } else {
                if (tagBuilder.length() > numQuotes) {
                    tagBuilder = new StringBuilder(); // Reset and keep on searching
                } else if (tagBuilder.length() == numQuotes) {
                    return i;
                } else if (tagBuilder.length() > 0) {
                    tagBuilder = new StringBuilder(); // Reset and keep on searching
                } // else (tabBuilder.length == 0) ==> continue
            }
        }

        // In case we arrive at the end of the text
        return tagBuilder.length() == numQuotes ? i : -1;
    }
}
