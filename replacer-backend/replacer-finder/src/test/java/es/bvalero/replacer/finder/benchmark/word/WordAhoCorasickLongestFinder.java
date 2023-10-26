package es.bvalero.replacer.finder.benchmark.word;

import com.roklenarcic.util.strings.LongestMatchMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.util.Collection;
import java.util.regex.MatchResult;

class WordAhoCorasickLongestFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    WordAhoCorasickLongestFinder(Collection<String> words) {
        this.stringMap = new LongestMatchMap<>(words, words, true);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        final ResultMatchListener listener = new ResultMatchListener();
        this.stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
