package es.bvalero.replacer.finder.benchmark.word;

import com.roklenarcic.util.strings.LongestMatchMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Finds the words/expressions in the text using the Aho-Corasick algorithm.
 * Then it checks the results to be complete in the text.
 * This approach matches the left-most longest non-overlapping occurrences of keywords.
 */
class WordAhoCorasickLongestFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    WordAhoCorasickLongestFinder(Collection<String> words) {
        this.stringMap = new LongestMatchMap<>(words, words, true);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final ResultMatchListener listener = new ResultMatchListener();
        this.stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match, page.getContent());
    }
}
