package es.bvalero.replacer.finder.benchmark.person;

import com.roklenarcic.util.strings.AhoCorasickMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

class PersonAhoCorasickFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    PersonAhoCorasickFinder(Collection<String> words) {
        this.stringMap = new AhoCorasickMap<>(words, words, true);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final ResultMatchListener listener = new ResultMatchListener();
        this.stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    @Override
    public boolean validate(MatchResult matchResult, FinderPage page) {
        return (
            FinderUtils.isWordCompleteInText(matchResult.start(), matchResult.group(), page.getContent()) &&
            FinderUtils.isWordFollowedByUpperCase(matchResult.start(), matchResult.group(), page.getContent())
        );
    }
}
