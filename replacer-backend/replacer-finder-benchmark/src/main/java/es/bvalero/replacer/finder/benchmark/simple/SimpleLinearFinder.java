package es.bvalero.replacer.finder.benchmark.simple;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.springframework.lang.Nullable;

class SimpleLinearFinder implements BenchmarkFinder {

    private final String word;

    SimpleLinearFinder(String word) {
        this.word = word;
    }

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
        return text.indexOf(word, start);
    }
}
