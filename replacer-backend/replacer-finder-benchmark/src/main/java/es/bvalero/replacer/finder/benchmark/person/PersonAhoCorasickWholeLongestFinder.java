package es.bvalero.replacer.finder.benchmark.person;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordLongestMatchMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Find all the person names in the text using the Aho-Corasick algorithm with whole-word longest match.
 * Then validate that the match is followed by an uppercase word.
 * No need to validate completeness: the whole-word matching already guarantees it.
 * This is the same approach used in production ({@link es.bvalero.replacer.finder.immutable.finders.PersonNameFinder}).
 */
class PersonAhoCorasickWholeLongestFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    PersonAhoCorasickWholeLongestFinder(Collection<String> words) {
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
        return FinderUtils.isWordFollowedByUpperCase(matchResult.start(), matchResult.group(), page.getContent());
    }
}
