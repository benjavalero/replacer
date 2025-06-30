package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class WordRegexAlternateCompleteSeparatorsFinder implements BenchmarkFinder {

    private final Pattern pattern;

    WordRegexAlternateCompleteSeparatorsFinder(Collection<String> words) {
        final String leftSeparator = "(?<![\\w_])";
        final String rightSeparator = "(?![\\w_])";
        final Iterable<String> cleanWords = words.stream().map(this::cleanWord).toList();
        final String alternate = '(' + FinderUtils.joinAlternate(cleanWords) + ')';
        final String regex = leftSeparator + alternate + rightSeparator;
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.pattern);
    }
}
