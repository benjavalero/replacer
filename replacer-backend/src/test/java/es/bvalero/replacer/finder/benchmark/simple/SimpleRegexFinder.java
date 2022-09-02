package es.bvalero.replacer.finder.benchmark.simple;

import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class SimpleRegexFinder implements BenchmarkFinder {

    private static final String SIMPLE_REGEX = SPACE;
    private static final Pattern SIMPLE_PATTERN = Pattern.compile(SIMPLE_REGEX);

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return RegexMatchFinder.find(page.getContent(), SIMPLE_PATTERN);
    }
}
