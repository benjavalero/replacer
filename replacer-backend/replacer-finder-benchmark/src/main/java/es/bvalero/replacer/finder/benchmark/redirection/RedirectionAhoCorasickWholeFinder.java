package es.bvalero.replacer.finder.benchmark.redirection;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordMatchMap;
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
 * This approach matches only whole word matches, i.e., keywords surrounded by non-word characters or string boundaries.
 * It doesn't allow non-word characters in the keywords.
 */
class RedirectionAhoCorasickWholeFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    RedirectionAhoCorasickWholeFinder(Collection<String> words) {
        char[] falseWordChars = { '-' };
        boolean[] wordCharFlags = { false };
        this.stringMap = new WholeWordMatchMap<>(words, words, false, falseWordChars, wordCharFlags);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final String text = page.getContent();
        final RedirectionMatchListener listener = new RedirectionMatchListener();
        this.stringMap.match(text, listener);
        return listener.isFound() ? Stream.of(FinderMatchRange.of(text, 0, text)) : Stream.of();
    }
}
