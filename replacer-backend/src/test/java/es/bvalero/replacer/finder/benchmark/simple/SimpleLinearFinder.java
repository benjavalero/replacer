package es.bvalero.replacer.finder.benchmark.simple;

import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.List;
import java.util.regex.MatchResult;

class SimpleLinearFinder implements BenchmarkFinder {

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findSpace);
    }

    private int findSpace(WikipediaPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();
        int startSpace = findStartSpace(text, start);
        if (startSpace >= 0) {
            matches.add(LinearMatchResult.of(startSpace, SPACE));
            return startSpace + 1;
        } else {
            return -1;
        }
    }

    private int findStartSpace(String text, int start) {
        return text.indexOf(SPACE, start);
    }
}
