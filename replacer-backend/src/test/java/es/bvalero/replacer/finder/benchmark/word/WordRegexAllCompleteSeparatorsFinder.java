package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

class WordRegexAllCompleteSeparatorsFinder implements BenchmarkFinder {

    private final Pattern wordPattern;
    private final Set<String> words = new HashSet<>();

    WordRegexAllCompleteSeparatorsFinder(Collection<String> words) {
        final String leftSeparator = "(?<![\\d_])";
        final String rightSeparator = "(?![\\d_])";
        final String wordRegex = "\\p{L}++";
        final String regex = leftSeparator + wordRegex + rightSeparator;
        this.wordPattern = Pattern.compile(regex);
        this.words.addAll(words);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return RegexMatchFinder.find(page.getContent(), wordPattern);
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        return this.words.contains(match.group());
    }
}
