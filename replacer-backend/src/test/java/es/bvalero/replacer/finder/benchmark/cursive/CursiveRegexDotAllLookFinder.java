package es.bvalero.replacer.finder.benchmark.cursive;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import es.bvalero.replacer.page.IndexablePage;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class CursiveRegexDotAllLookFinder implements BenchmarkFinder {

    private static final String TWO_QUOTES_ONLY = "(?<!')''(?!')";
    private static final String CURSIVE_REGEX = "%s.*?(%s|\n)";
    private static final Pattern CURSIVE_PATTERN = Pattern.compile(
        String.format(CURSIVE_REGEX, TWO_QUOTES_ONLY, TWO_QUOTES_ONLY)
    );

    @Override
    public Iterable<MatchResult> findMatchResults(IndexablePage page) {
        return RegexMatchFinder.find(page.getContent(), CURSIVE_PATTERN);
    }
}
