package es.bvalero.replacer.finder.benchmark.redirection;

import com.roklenarcic.util.strings.AhoCorasickMap;
import com.roklenarcic.util.strings.MapMatchListener;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.Getter;

class RedirectionAhoCorasickFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    RedirectionAhoCorasickFinder(Collection<String> words) {
        this.stringMap = new AhoCorasickMap<>(words, words, false);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        final ResultMatchListener listener = new ResultMatchListener();
        this.stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    @Getter
    private static class ResultMatchListener implements MapMatchListener<String> {

        private final List<MatchResult> matches = new ArrayList<>(1);

        @Override
        public boolean match(String text, int start, int end, String word) {
            matches.add(LinearMatchResult.of(0, text));
            return false;
        }
    }
}
