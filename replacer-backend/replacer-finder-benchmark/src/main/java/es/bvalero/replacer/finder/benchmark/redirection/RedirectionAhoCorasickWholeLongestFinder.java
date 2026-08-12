package es.bvalero.replacer.finder.benchmark.redirection;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordLongestMatchMap;
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
 * This approach matches only whole word matches, i.e., keywords surrounded by non-word characters or string boundaries.
 * It allows non-word characters in the keywords.
 * This means there can be overlaps, in which case the leftmost longest match will be returned.
 */
class RedirectionAhoCorasickWholeLongestFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    RedirectionAhoCorasickWholeLongestFinder(Collection<String> words) {
        char[] falseWordChars = { '-' };
        boolean[] wordCharFlags = { false };
        this.stringMap = new WholeWordLongestMatchMap<>(words, words, false, falseWordChars, wordCharFlags);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final String text = page.getContent();
        final RedirectionMatchListener listener = new RedirectionMatchListener();
        this.stringMap.match(text, listener);
        return listener.isFound() ? Stream.of(FinderMatchResult.of(0, text)) : Stream.of();
    }
}
