package es.bvalero.replacer.finder.benchmark.redirection;

import com.roklenarcic.util.strings.AhoCorasickMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.immutable.finders.RedirectionMatchListener;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Finds the redirection words in the text using the Aho-Corasick algorithm.
 * Returns the whole page on first match.
 * Case-insensitive: delegates to the library ({@code caseSensitive=false}).
 * This is the simplest approach. It matches all occurrences of all strings, possibly overlapping.
 */
class RedirectionAhoCorasickFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    RedirectionAhoCorasickFinder(Collection<String> words) {
        this.stringMap = new AhoCorasickMap<>(words, words, false);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final String text = page.getContent();
        final RedirectionMatchListener listener = new RedirectionMatchListener();
        this.stringMap.match(text, listener);
        return listener.isFound() ? Stream.of(FinderMatchResult.of(0, text)) : Stream.of();
    }
}
