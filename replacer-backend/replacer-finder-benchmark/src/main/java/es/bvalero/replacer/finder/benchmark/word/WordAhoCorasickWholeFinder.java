package es.bvalero.replacer.finder.benchmark.word;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordMatchMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.util.Collection;
import java.util.regex.MatchResult;

/**
 * Finds the words/expressions in the text using the Aho-Corasick algorithm.
 * There is no need to validate if the results are complete in the text.
 * This approach matches only whole word matches, i.e., keywords surrounded by non-word characters or string boundaries.
 * It doesn't allow non-word characters in the keywords.
 */
class WordAhoCorasickWholeFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    WordAhoCorasickWholeFinder(Collection<String> words) {
        char[] falseWordChars = { '-' };
        boolean[] wordCharFlags = { false };
        this.stringMap = new WholeWordMatchMap<>(words, words, true, falseWordChars, wordCharFlags);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        final ResultMatchListener listener = new ResultMatchListener();
        this.stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }
    // No need to validate
}
