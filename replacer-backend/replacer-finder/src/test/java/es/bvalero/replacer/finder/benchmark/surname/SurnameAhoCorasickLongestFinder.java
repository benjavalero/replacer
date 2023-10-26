package es.bvalero.replacer.finder.benchmark.surname;

import com.roklenarcic.util.strings.LongestMatchMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.util.Collection;
import java.util.regex.MatchResult;

class SurnameAhoCorasickLongestFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    SurnameAhoCorasickLongestFinder(Collection<String> words) {
        this.stringMap = new LongestMatchMap<>(words, words, true);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        final ResultMatchListener listener = new ResultMatchListener();
        this.stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    @Override
    public boolean validate(MatchResult matchResult, FinderPage page) {
        return (
            FinderUtils.isWordCompleteInText(matchResult.start(), matchResult.group(), page.getContent()) &&
            FinderUtils.isWordPrecededByUpperCase(matchResult.start(), page.getContent())
        );
    }
}
