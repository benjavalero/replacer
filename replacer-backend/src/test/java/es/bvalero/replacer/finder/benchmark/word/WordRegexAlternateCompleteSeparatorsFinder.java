package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Collection;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class WordRegexAlternateCompleteSeparatorsFinder implements BenchmarkFinder {

    private final Pattern pattern;

    WordRegexAlternateCompleteSeparatorsFinder(Collection<String> words) {
        final String leftSeparator = "(?<![\\p{L}_/.]|\\w')";
        final String rightSeparator = "(?![_/])";
        final String alternate = '(' + FinderUtils.joinAlternate(words) + ')';
        final String regex = leftSeparator + alternate + rightSeparator;
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return RegexMatchFinder.find(page.getContent(), this.pattern);
    }
}
