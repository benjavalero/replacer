package es.bvalero.replacer.finder.benchmark.cursive;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class CursiveRegexDotLazyFinder implements BenchmarkFinder {

    private static final String TWO_QUOTES_ONLY = "[^']''[^']";
    private static final String CURSIVE_REGEX = "%s.*?(%s|\n)";
    private static final Pattern CURSIVE_PATTERN = Pattern.compile(
        String.format(CURSIVE_REGEX, TWO_QUOTES_ONLY, TWO_QUOTES_ONLY)
    );

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), CURSIVE_PATTERN);
    }

    @Override
    public BenchmarkResult convert(MatchResult match) {
        int start = match.start() + 1;
        int end = match.group().endsWith("\n") ? match.group().length() : match.group().length() - 1;
        String group = match.group().substring(1, end);
        return BenchmarkResult.of(start, group);
    }
}
