package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Loop over all the words/expressions and find them in the text with a regex.
 * The regex contains the word/expression surrounded by word boundaries or underscores.
 * No need to validate: the separators already guarantee completeness.
 * Con: it iterates over all the words/expressions, so it may be slow for large collections.
 */
class WordRegexCompleteSeparatorsFinder implements BenchmarkFinder {

    private final List<Pattern> patterns;

    WordRegexCompleteSeparatorsFinder(Collection<String> words) {
        this.patterns = new ArrayList<>();
        for (String word : words) {
            final String leftSeparator = "(?<!%s)".formatted(SEPARATOR_CLASS);
            final String rightSeparator = "(?!%s)".formatted(SEPARATOR_CLASS);
            final String regex = leftSeparator + cleanWord(word) + rightSeparator;
            this.patterns.add(Pattern.compile(regex));
        }
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return patterns.stream().flatMap(p -> RegexMatchFinder.find(page.getContent(), p));
    }
    // No need to validate
}
