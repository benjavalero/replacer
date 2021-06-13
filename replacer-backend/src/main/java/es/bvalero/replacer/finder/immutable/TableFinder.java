package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.stereotype.Component;

/**
 * Find table-related immutables:
 * <ul>
 *     <li>Find table styles, i.e. lines starting with <code>{|</code</li>
 *     <li>Find row style, i.e. lines starting with <code>|-</code></li>
 * </ul>
 */
@Component
class TableFinder extends ImmutableCheckedFinder {

    private static final String TABLE_START = "{|";
    private static final String ROW_START = "|-";

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return findAllTableStyles(page.getContent());
    }

    private List<MatchResult> findAllTableStyles(String text) {
        List<MatchResult> tableLines = new ArrayList<>();
        int startLine = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                if (startLine >= 0) {
                    // Finish line
                    String line = text.substring(startLine, i);
                    if (isImmutableLine(line)) {
                        tableLines.add(LinearMatchResult.of(startLine, line));
                    }
                }
                startLine = -1;
            } else if (startLine < 0) {
                startLine = i;
            }
        }
        return tableLines;
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
