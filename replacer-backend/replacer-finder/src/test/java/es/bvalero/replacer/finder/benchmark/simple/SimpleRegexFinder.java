package es.bvalero.replacer.finder.benchmark.simple;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class SimpleRegexFinder implements BenchmarkFinder {

    private final Pattern pattern;

    SimpleRegexFinder(String word) {
        this.pattern = Pattern.compile(word);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), pattern);
    }
}
