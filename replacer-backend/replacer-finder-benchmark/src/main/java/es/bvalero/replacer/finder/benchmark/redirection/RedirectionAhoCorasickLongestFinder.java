package es.bvalero.replacer.finder.benchmark.redirection;

import com.roklenarcic.util.strings.LongestMatchMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.immutable.finders.RedirectionMatchListener;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Finds the redirection words in the text using the Aho-Corasick algorithm.
 * Returns the whole page on first match.
 * Case-insensitive: delegates to the library ({@code caseSensitive=false}).
 * This approach matches the leftmost longest non-overlapping occurrences of keywords.
 */
class RedirectionAhoCorasickLongestFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    RedirectionAhoCorasickLongestFinder(Collection<String> words) {
        this.stringMap = new LongestMatchMap<>(words, words, false);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final String text = page.getContent();
        final RedirectionMatchListener listener = new RedirectionMatchListener();
        this.stringMap.match(text, listener);
        return listener.isFound() ? Stream.of(FinderMatchRange.of(text, 0, text)) : Stream.of();
    }
}
