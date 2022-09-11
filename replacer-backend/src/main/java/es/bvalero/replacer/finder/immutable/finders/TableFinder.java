package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
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

    private static final char NEW_LINE = '\n';
    private static final String TABLE_START = "{|";
    private static final String ROW_START = "|-";

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findLine);
    }

    @Nullable
    MatchResult findLine(WikipediaPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startLine = findStartLine(text, start);
            if (startLine >= 0) {
                final int endLine = findEndLine(text, startLine);
                // Take into account the end of file
                final String line = endLine >= 0 ? text.substring(startLine, endLine) : text.substring(startLine);

                if (isImmutableLine(line)) {
                    return LinearMatchResult.of(startLine, line);
                } else {
                    start = endLine;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private int findStartLine(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (text.charAt(i) != NEW_LINE) {
                return i;
            }
        }
        return -1;
    }

    private int findEndLine(String text, int start) {
        return text.indexOf(NEW_LINE, start);
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
