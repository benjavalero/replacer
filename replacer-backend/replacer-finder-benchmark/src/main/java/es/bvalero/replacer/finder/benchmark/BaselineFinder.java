package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.UUID;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.springframework.lang.Nullable;

public class BaselineFinder implements BenchmarkFinder {

    private final String word = UUID.randomUUID().toString();

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
                return FinderMatchResult.of(startMatch, word);
            } else {
                return null;
            }
        }
        return null;
    }

    private int findStartMatch(String text, int start) {
        // return text.charAt(1) == 'ñ' ? 1 : -1;
        return text.indexOf(word, start);
    }
}
