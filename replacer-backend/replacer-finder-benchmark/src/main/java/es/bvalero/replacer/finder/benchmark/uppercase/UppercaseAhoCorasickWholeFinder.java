package es.bvalero.replacer.finder.benchmark.uppercase;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordMatchMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Find the words in the text using the Aho-Corasick algorithm with whole-word matching.
 * Then validate that the match is preceded by the expected punctuation.
 * Con: unlike {@link UppercaseAhoCorasickWholeLongestFinder}, this one does not use longest matching,
 * so it may return shorter matches when words share a prefix.
 */
class UppercaseAhoCorasickWholeFinder extends UppercaseBenchmarkFinder {

    private final StringMap<String> stringMap;

    UppercaseAhoCorasickWholeFinder(Collection<String> words) {
        char[] falseWordChars = { '-' };
        boolean[] wordCharFlags = { false };
        this.stringMap = new WholeWordMatchMap<>(words, words, true, falseWordChars, wordCharFlags);
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
