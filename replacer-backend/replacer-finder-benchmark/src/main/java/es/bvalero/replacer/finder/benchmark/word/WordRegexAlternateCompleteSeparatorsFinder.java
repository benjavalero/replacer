package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Collection;
import java.util.Comparator;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Find all the words/expressions in the text with a regex.
 * The regex contains the alternation of all the words/expressions surrounded by word boundaries or underscores.
 * Then there is no need to check if the result is complete in the text.
 */
class WordRegexAlternateCompleteSeparatorsFinder implements BenchmarkFinder {

    private final Pattern pattern;

    WordRegexAlternateCompleteSeparatorsFinder(Collection<String> words) {
        final String leftSeparator = "(?<!%s)".formatted(SEPARATOR_CLASS);
        final String rightSeparator = "(?!%s)".formatted(SEPARATOR_CLASS);
        final Iterable<String> cleanWords = words
            .stream()
            .sorted(Comparator.reverseOrder())
            .map(this::cleanWord)
            .toList();
        final String alternate = '(' + FinderUtils.joinAlternate(cleanWords) + ')';
        final String regex = leftSeparator + alternate + rightSeparator;
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), this.pattern);
    }
}
