package es.bvalero.replacer.finder.benchmark.simple;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

class SimpleLinearFinder implements BenchmarkFinder {

    private final String word;

    SimpleLinearFinder(String word) {
        this.word = word;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findMatch);
    }

    @Nullable
    private MatchResult findMatch(WikipediaPage page, int start) {
        final String text = page.getContent();
        if (start >= 0 && start < text.length()) {
            final int startMatch = findStartMatch(text, start);
            if (startMatch >= 0) {
                return LinearMatchResult.of(startMatch, word);
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
