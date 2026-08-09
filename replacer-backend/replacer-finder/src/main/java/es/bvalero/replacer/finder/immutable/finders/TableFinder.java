package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.NEW_LINE;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find table-related styles:
 * <ul>
 *     <li>Table styles, i.e. lines starting with <code>{|</code</li>
 *     <li>Row style, i.e. lines starting with <code>|-</code></li>
 * </ul>
 */
@Component
public class TableFinder implements ImmutableFinder {

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
            if (!isImmutableLine(text, startLine, endLine)) {
                start = endLine;
                continue;
            }

            return FinderMatchRange.of(text, startLine, endLine);
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

    private boolean isImmutableLine(String text, int start, int end) {
        return isStartTableLine(text, start) || isStartRowLine(text, start);
    }

    private boolean isStartTableLine(String text, int start) {
        return ReplacerUtils.containsAtPosition(text, TABLE_START, start);
    }

    private boolean isStartRowLine(String text, int start) {
        return ReplacerUtils.containsAtPosition(text, ROW_START, start);
    }
}
