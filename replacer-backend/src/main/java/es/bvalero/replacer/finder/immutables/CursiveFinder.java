package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.page.IndexablePage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find text in cursive and bold, e.g. `''cursive''` in `This is a ''cursive'' example`
 * It also finds text starting with the simple quotes and ending with a new line.
 */
@Slf4j
@Component
public class CursiveFinder implements ImmutableFinder {

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 2000;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new LinearIterable<>(page, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findCursive(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findCursive(IndexablePage page, int start, List<MatchResult> matches) {
        String text = page.getContent();
        int startCursive = findStartCursive(text, start);
        if (startCursive >= 0) {
            int numQuotes = findNumQuotes(text, startCursive);
            int endQuotes = findEndQuotes(text, startCursive + numQuotes, numQuotes);
            if (endQuotes >= 0) {
                if (StringUtils.isBlank(text.substring(startCursive + numQuotes, endQuotes))) {
                    Immutable immutable = Immutable.of(
                        startCursive,
                        FinderUtils.getContextAroundWord(text, startCursive, endQuotes, getContextThreshold()),
                        this
                    );
                    logWarning(immutable, page, LOGGER, "Empty cursive");
                } else {
                    matches.add(LinearMatcher.of(startCursive, text.substring(startCursive, endQuotes)));
                }
                return endQuotes;
            } else {
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
