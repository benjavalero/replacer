package es.bvalero.replacer.finder.benchmark.redirection;

import com.roklenarcic.util.strings.AhoCorasickMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.immutable.finders.RedirectionMatchListener;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Finds the redirection words in the lowercase text using the Aho-Corasick algorithm.
 * Returns the whole page on first match.
 * Case-insensitive: normalizes keywords to lowercase in the constructor and converts the text to lowercase at search time.
 * This is the simplest approach. It matches all occurrences of all strings, possibly overlapping.
 */
class RedirectionLowercaseAhoCorasickFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    RedirectionLowercaseAhoCorasickFinder(Collection<String> words) {
        List<String> lowerCaseWords = words.stream().map(ReplacerUtils::toLowerCase).toList();
        this.stringMap = new AhoCorasickMap<>(lowerCaseWords, lowerCaseWords, true);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final String text = page.getContent();
        final RedirectionMatchListener listener = new RedirectionMatchListener();
        this.stringMap.match(ReplacerUtils.toLowerCase(text), listener);
        return listener.isFound() ? Stream.of(FinderMatchRange.of(text, 0, text)) : Stream.of();
    }
}
