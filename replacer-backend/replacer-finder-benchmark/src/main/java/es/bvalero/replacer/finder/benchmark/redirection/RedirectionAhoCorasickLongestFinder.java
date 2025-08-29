package es.bvalero.replacer.finder.benchmark.redirection;

import com.roklenarcic.util.strings.LongestMatchMap;
import com.roklenarcic.util.strings.MapMatchListener;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

class RedirectionAhoCorasickLongestFinder implements BenchmarkFinder {

    private final StringMap<String> stringMap;

    RedirectionAhoCorasickLongestFinder(Collection<String> words) {
        this.stringMap = new LongestMatchMap<>(words, words, false);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final ResultMatchListener listener = new ResultMatchListener();
        this.stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    private static class ResultMatchListener implements MapMatchListener<String> {

        private final List<MatchResult> matches = new ArrayList<>(1);

        private Stream<MatchResult> getMatches() {
            return matches.stream();
        }

        @Override
        public boolean match(String text, int start, int end, String word) {
            matches.add(FinderMatchResult.of(0, text));
            return false;
        }
    }
}
