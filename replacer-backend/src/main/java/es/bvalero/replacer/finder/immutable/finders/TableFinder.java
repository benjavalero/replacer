package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find table-related styles:
 * <ul>
 *     <li>Table styles, i.e. lines starting with <code>{|</code</li>
 *     <li>Row style, i.e. lines starting with <code>|-</code></li>
 * </ul>
 */
@Component
class TableFinder implements ImmutableFinder {

    private static final String TABLE_START = "{|";
    private static final String ROW_START = "|-";

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findResult);
    }

    @Nullable
    private MatchResult findResult(FinderPage page, int start) {
        final List<MatchResult> matches = new ArrayList<>();
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findLine(page, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findLine(FinderPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        final int startLine = findStartLine(text, start);
        if (startLine >= 0) {
            final int endLine = findEndLine(text, startLine);
            final String line;
            if (endLine >= 0) {
                line = text.substring(startLine, endLine);
            } else {
                // End of file
                line = text.substring(startLine);
            }

            if (isImmutableLine(line)) {
                matches.add(LinearMatchResult.of(startLine, line));
            }
            return endLine;
        } else {
            return -1;
        }
    }

    private int findStartLine(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (text.charAt(i) != '\n') {
                return i;
            }
        }
        return -1;
    }

    private int findEndLine(String text, int start) {
        return text.indexOf('\n', start);
    }

    private boolean isImmutableLine(String line) {
        return isStartTableLine(line) || isStartRowLine(line);
    }

    private boolean isStartTableLine(String line) {
        return line.startsWith(TABLE_START);
    }

    private boolean isStartRowLine(String line) {
        return line.startsWith(ROW_START);
    }
}
