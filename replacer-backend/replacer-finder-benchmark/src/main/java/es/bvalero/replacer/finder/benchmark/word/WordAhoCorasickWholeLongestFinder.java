package es.bvalero.replacer.finder.benchmark.word;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordLongestMatchMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Finds the words/expressions in the text using the Aho-Corasick algorithm.
 * There is no need to validate if the results are complete in the text.
 * This approach matches only whole word matches, i.e., keywords surrounded by non-word characters or string boundaries.
 * It allows non-word characters in the keywords.
 * This means there can be overlaps, in which case the leftmost longest match will be returned.
 */
class WordAhoCorasickWholeLongestFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    WordAhoCorasickWholeLongestFinder(Collection<String> words) {
        char[] falseWordChars = { '-', '№', '\'', '[', ']' };
        boolean[] wordCharFlags = { false, true, true, true, true };
        this.stringMap = new WholeWordLongestMatchMap<>(words, words, true, falseWordChars, wordCharFlags);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final ResultMatchListener listener = new ResultMatchListener();
        this.stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }
    // No need to validate
}
