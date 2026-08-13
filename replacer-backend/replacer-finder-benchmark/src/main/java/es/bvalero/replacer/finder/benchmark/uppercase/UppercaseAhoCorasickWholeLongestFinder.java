package es.bvalero.replacer.finder.benchmark.uppercase;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordLongestMatchMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Find the words in the text using the Aho-Corasick algorithm with whole-word longest matching.
 * Then validate that the match is preceded by the expected punctuation.
 * This is the strategy used in production.
 */
class UppercaseAhoCorasickWholeLongestFinder extends UppercaseBenchmarkFinder {

    private final StringMap<String> stringMap;

    UppercaseAhoCorasickWholeLongestFinder(Collection<String> words) {
        char[] falseWordChars = { '-' };
        boolean[] wordCharFlags = { false };
        this.stringMap = new WholeWordLongestMatchMap<>(words, words, true, falseWordChars, wordCharFlags);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final ResultMatchListener listener = new ResultMatchListener();
        this.stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    @Override
    public boolean validate(MatchResult matchResult, FinderPage page) {
        return isWordPrecededByPunctuation(matchResult.start(), page.getContent());
    }
}
