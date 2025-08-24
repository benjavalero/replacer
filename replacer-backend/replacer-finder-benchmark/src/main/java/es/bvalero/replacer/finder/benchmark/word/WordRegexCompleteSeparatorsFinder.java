package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Loop over all the words/expressions and find them in the text with a regex.
 * The regex contains the word/expression surrounded by word boundaries or underscores.
 * Then there is no need to check if the result is complete in the text.
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
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (Pattern pattern : this.patterns) {
            final Matcher m = pattern.matcher(text);
            while (m.find()) {
                final int start = m.start();
                final String word = m.group();
                matches.add(BenchmarkResult.of(start, word));
            }
        }
        return matches.stream();
    }
}
