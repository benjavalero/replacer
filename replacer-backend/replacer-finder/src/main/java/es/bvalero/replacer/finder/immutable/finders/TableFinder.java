package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.NEW_LINE;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
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
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findLine);
    }

    @Nullable
    private MatchResult findLine(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final int startLine = findStartLine(text, start);
            if (startLine < 0) {
                return null;
            }

            final int endLine = findEndLine(text, startLine);
            final String line = text.substring(startLine, endLine);
            if (!isImmutableLine(line)) {
                start = endLine;
                continue;
            }

            return FinderMatchResult.of(startLine, line);
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
        final int endLine = text.indexOf(NEW_LINE, start);
        return endLine >= 0 ? endLine : text.length();
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
