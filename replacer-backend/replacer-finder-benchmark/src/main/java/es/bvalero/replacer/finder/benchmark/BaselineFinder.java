package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.springframework.lang.Nullable;

public class BaselineFinder implements BenchmarkFinder {

    private static final char CHAR = '~'; // Character rare enough in Spanish articles
    private static final String WORD = String.valueOf(CHAR);

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findMatch);
    }

    @Nullable
    private MatchResult findMatch(FinderPage page, int start) {
        final String text = page.getContent();
        if (start >= 0 && start < text.length()) {
            final int startMatch = findStartMatch(text, start);
            if (startMatch >= 0) {
                return FinderMatchResult.of(startMatch, WORD);
            } else {
                return null;
            }
        }
        return null;
    }

    private int findStartMatch(String text, int start) {
        // return text.isEmpty() ? 1 : -1;
        return text.indexOf(CHAR, start);
    }
}
